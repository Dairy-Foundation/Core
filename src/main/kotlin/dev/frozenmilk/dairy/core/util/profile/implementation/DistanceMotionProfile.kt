package dev.frozenmilk.dairy.core.util.profile.implementation

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.util.profile.MotionProfile
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.cell.LazyCell
import dev.frozenmilk.util.units.distance.Distance
import dev.frozenmilk.util.units.distance.DistanceUnit
import dev.frozenmilk.util.units.distance.DistanceUnits
import kotlin.math.abs
import kotlin.random.Random

class DistanceMotionProfile(beginState: State) : MotionProfile<Distance>, Feature {
    override var dependency: Dependency<*> = Dependency { _, _, _ -> }

    val beginState: State
    private val accelSegments = ArrayList<AccelSegment>()
    var endState: State
        private set
    var totalDuration: Double = 0.0
        private set
    private val startTimestampCell = LazyCell { System.nanoTime() }
    val startTimestamp: Long by startTimestampCell
    private val currentStateCell = LazyCell { get() }
    val currentState by currentStateCell
    var limitless: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                currentStateCell.invalidate()
            }
        }

    init {
        this.beginState = State(beginState.position, beginState.velocity)
        this.endState = this.beginState
    }

    class State(
        position: Distance,
        velocity: Distance,
        acceleration: Distance = Distance()
    ) : MotionProfile.State<Distance> {
        init {
            if (position.isNaN()) {
                throw IllegalArgumentException("position of DistanceMotionProfile.State must not be NaN.")
            }
            if (velocity.isNaN()) {
                throw IllegalArgumentException("velocity of DistanceMotionProfile.State must not be NaN.")
            }
            if (acceleration.isNaN()) {
                throw IllegalArgumentException("acceleration of DistanceMotionProfile.State must not be NaN.")
            }
        }

        override val position = position.intoCommon()
        override val velocity = velocity.intoCommon()
        override val acceleration = acceleration.intoCommon()

        operator fun unaryPlus() = this
        operator fun unaryMinus() = State(
            -position, -velocity, -acceleration
        )

        operator fun plus(other: State) = State(
            position + other.position,
            velocity + other.velocity,
            acceleration + other.acceleration
        )
        operator fun minus(other: State) = State(
            position - other.position,
            velocity - other.velocity,
            acceleration - other.acceleration
        )

        operator fun times(scalar: Double) = State(
            position * scalar,
            velocity * scalar,
            acceleration * scalar
        )
        operator fun div(scalar: Double) = State(
            position / scalar,
            velocity / scalar,
            acceleration / scalar
        )

        fun obeys(constraints: Constraints): Boolean = (velocity.absoluteValue <= constraints.maxVelocity)

        operator fun invoke(accelSegment: AccelSegment, reversed: Boolean = false): State {
            val multi: Double = if (reversed) -1.0 else 1.0
            return State(
                position + velocity * accelSegment.time * multi + accelSegment.acceleration * accelSegment.time * accelSegment.time / 2.0,
                velocity + accelSegment.acceleration * accelSegment.time * multi,
                accelSegment.acceleration
            )
        }
        operator fun invoke(profile: DistanceMotionProfile, reversed: Boolean = false): State {
            var state = this
            if (reversed) {
                for (i in profile.accelSegments.size-1 downTo 0) {
                    state = state(profile.accelSegments[i], reversed = true)
                }
            }
            else {
                for (segment in profile.accelSegments) {
                    state = state(segment)
                }
            }
            return state
        }
    }

    class Constraints(
        maxVelocity: Distance,
        maxAcceleration: Distance,
        maxDeceleration: Distance
    ) {
        init {
            if (maxVelocity.isNaN() || maxVelocity <= Distance()) {
                throw IllegalArgumentException("maxVelocity of DistanceMotionProfile.Constraints must be positive.")
            }
            if (maxAcceleration.isNaN() || maxAcceleration <= Distance()) {
                throw IllegalArgumentException("maxAcceleration of DistanceMotionProfile.Constraints must be positive.")
            }
            if (maxDeceleration.isNaN() || maxDeceleration <= Distance()) {
                throw IllegalArgumentException("maxDeceleration of DistanceMotionProfile.Constraints must be positive.")
            }
        }

        val maxVelocity = maxVelocity.intoCommon()
        val maxAcceleration = maxAcceleration.intoCommon()
        val maxDeceleration = maxDeceleration.intoCommon()
    }

    class AccelSegment(
        acceleration: Distance,
        val time: Double
    ) {
        constructor() : this(Distance(), 0.0)
        init {
            if (acceleration.isNaN()) {
                throw IllegalArgumentException("acceleration of DistanceMotionProfile.AccelSegment must not be NaN.")
            }
            if (time.isNaN() || (debugMode && time < 0.0)) {
                throw IllegalArgumentException("time of DistanceMotionProfile.AccelSegment must be non-negative.")
            }
        }

        val acceleration = acceleration.intoCommon()

        operator fun unaryPlus() = this
        operator fun unaryMinus() = AccelSegment(
            -acceleration, time
        )
    }


    private operator fun unaryPlus() = this
    private operator fun unaryMinus(): DistanceMotionProfile {
        val profile = DistanceMotionProfile(-beginState)
        for (segment in accelSegments) {
            profile += -segment
        }
        profile.endState = -endState
        return profile
    }

    private operator fun plusAssign(accelSegment: AccelSegment) {
        if (accelSegment.time <= 0.0) return
        accelSegments.add(accelSegment)
        endState = endState(accelSegment).let { State(it.position, it.velocity) }
        totalDuration += accelSegment.time
    }
    private operator fun minusAssign(accelSegment: AccelSegment) {
        plusAssign(-accelSegment)
    }

    private operator fun plusAssign(profile: DistanceMotionProfile) {
        for (segment in profile.accelSegments) {
            plusAssign(segment)
        }
    }
    private operator fun minusAssign(profile: DistanceMotionProfile) {
        for (segment in profile.accelSegments) {
            minusAssign(segment)
        }
    }


    private fun getInBounds(t: Double): State {
        /// promise: 0 <= t <= totalDuration
        var state = beginState;
        for (segment in accelSegments) {
            if (t <= segment.time) {
                return state(AccelSegment(segment.acceleration, t))
            }
            state = state(segment)
        }
        return endState
    }

    override fun getLimited(t: Double): State {
        if (t <= 0.0) {
            return beginState
        }
        if (t >= totalDuration) {
            return endState
        }
        return getInBounds(t)
    }
    override fun getLimited() = getLimited((System.nanoTime() - startTimestamp).toDouble() / 1e9)

    override fun getLimitless(t: Double): State {
        if (t <= 0.0) {
            return State(beginState.position + beginState.velocity * t, beginState.velocity)
        }
        if (t >= totalDuration) {
            return State(endState.position + endState.velocity * (t - totalDuration), endState.velocity)
        }
        return getInBounds(t)
    }
    override fun getLimitless() = getLimitless((System.nanoTime() - startTimestamp).toDouble() / 1e9)

    override fun get(t: Double): State {
        return if (limitless) getLimitless(t) else getLimited(t)
    }
    override fun get() = get((System.nanoTime() - startTimestamp).toDouble() / 1e9)

    override fun get(motionComponent: MotionComponents): Distance {
        return when(motionComponent) {
            MotionComponents.STATE -> currentState.position
            MotionComponents.VELOCITY, MotionComponents.RAW_VELOCITY -> currentState.velocity
            MotionComponents.ACCELERATION, MotionComponents.RAW_ACCELERATION -> currentState.acceleration
        }
    }

    override fun reset() {
        startTimestampCell.invalidate()
        currentStateCell.invalidate()
    }

    override fun postUserInitLoopHook(opMode: Wrapper) {
        currentStateCell.invalidate()
    }
    override fun postUserLoopHook(opMode: Wrapper) {
        currentStateCell.invalidate()
    }


    companion object {
        fun generateVelProfile(
            beginState: State,
            endVel: Distance,
            constraints: Constraints,
            limitless: Boolean = false
        ): DistanceMotionProfile {
            return (if (beginState.velocity >= Distance())
                generateVelProfileNonNegativeBeginVel(beginState, endVel.intoCommon(), constraints)
            else -generateVelProfileNonNegativeBeginVel(-beginState, -endVel.intoCommon(), constraints)
                    ).also {
                it.limitless = limitless
                if (!debugMode) it.endState = State(it.endState.position, endVel)
            }
        }

        fun generateVelProfile(
            beginVel: Distance,
            endVel: Distance,
            constraints: Constraints,
            limitless: Boolean = false
        ) = generateVelProfile(State(Distance(), beginVel), endVel, constraints, limitless)

        private fun generateVelProfileNonNegativeBeginVel(
            beginState: State,
            endVel: Distance,
            constraints: Constraints
        ): DistanceMotionProfile {
            /// promise: beginState.velocity is non-negative, endVel is in common unit
            val profile = DistanceMotionProfile(beginState)
            if (beginState.velocity == endVel) return profile
            if (beginState.velocity < endVel) {
                profile += AccelSegment(constraints.maxAcceleration, ((endVel - beginState.velocity) / constraints.maxAcceleration).value)
                return profile
            }
            if (endVel >= Distance()) {
                profile += AccelSegment(-constraints.maxDeceleration, ((beginState.velocity - endVel) / constraints.maxDeceleration).value)
                return profile
            }
            profile += AccelSegment(-constraints.maxDeceleration, (beginState.velocity / constraints.maxDeceleration).value)
            profile += AccelSegment(-constraints.maxAcceleration, (-endVel / constraints.maxAcceleration).value)
            return profile
        }


        fun generateProfile(
            beginState: State,
            endState: State,
            constraints: Constraints,
            limitless: Boolean = false
        ): DistanceMotionProfile {
            val seg1 = if (beginState.obeys(constraints)) AccelSegment() else AccelSegment(
                -constraints.maxDeceleration * beginState.velocity.sign,
                ((beginState.velocity.absoluteValue - constraints.maxVelocity) / constraints.maxDeceleration).value
            )
            val seg2 = if (endState.obeys(constraints)) AccelSegment() else AccelSegment(
                constraints.maxAcceleration * endState.velocity.sign,
                ((endState.velocity.absoluteValue - constraints.maxVelocity) / constraints.maxAcceleration).value
            )
            val profile = DistanceMotionProfile(beginState)
            profile += seg1
            profile += generateProfileObeying(
                beginState(seg1).let { State(it.position, it.velocity.coerceIn(-constraints.maxVelocity, constraints.maxVelocity)) },
                endState(seg2, reversed = true).let { State(it.position, it.velocity.coerceIn(-constraints.maxVelocity, constraints.maxVelocity)) },
                constraints
            )
            profile += seg2
            profile.limitless = limitless
            if (!debugMode) profile.endState = State(endState.position, endState.velocity)
            return profile
        }

        private fun generateProfileObeying(
            beginState: State,
            endState: State,
            constraints: Constraints
        ): DistanceMotionProfile {
            /// promise: beginState and endState are within constraints
            val velProfileBeginEnd = generateVelProfile(beginState.velocity, endState.velocity, constraints)
            val velProfileBeginZero = generateVelProfile(beginState.velocity, Distance(), constraints)
            val velProfileZeroEnd = generateVelProfile(Distance(), endState.velocity, constraints)

            val profileBegin = DistanceMotionProfile(beginState)
            profileBegin += generateSimpleProfile(
                beginState.velocity,
                endState(velProfileBeginEnd, reversed = true).position - beginState.position,
                constraints
            )
            profileBegin += velProfileBeginEnd

            val profileEnd = DistanceMotionProfile(beginState)
            profileEnd += velProfileBeginEnd
            profileEnd += generateSimpleProfile(
                endState.velocity,
                endState.position - beginState(velProfileBeginEnd).position,
                constraints
            )

            val profileZero = DistanceMotionProfile(beginState)
            profileZero += velProfileBeginZero
            profileZero += generateSimpleProfile(
                Distance(),
                endState(velProfileZeroEnd, reversed = true).position - beginState(velProfileBeginZero).position,
                constraints
            )
            profileZero += velProfileZeroEnd

            var bestProfile = profileBegin
            if (profileEnd.totalDuration < bestProfile.totalDuration) bestProfile = profileEnd
            if (profileZero.totalDuration < bestProfile.totalDuration) bestProfile = profileZero
            return bestProfile
        }


        private fun generateSimpleProfile(
            beginEndVel: Distance,
            distance: Distance,
            constraints: Constraints
        ): DistanceMotionProfile {
            /// promise: beginEndVel obeys constraints, beginEndVel and distance are in common unit
            return if (distance >= Distance())
                generateSimpleProfileNonNegativeDistance(beginEndVel, distance, constraints)
            else -generateSimpleProfileNonNegativeDistance(-beginEndVel, -distance, constraints)
        }

        private fun generateSimpleProfileNonNegativeDistance(
            beginEndVel: Distance,
            distance: Distance,
            constraints: Constraints
        ): DistanceMotionProfile {
            /// promise: beginEndVel obeys constraints, distance is non-negative, beginEndVel and distance are in common unit
            if (distance == Distance()) return DistanceMotionProfile(State(Distance(), beginEndVel))
            if (beginEndVel >= Distance()) {
                return generateSimpleProfileNonNegativeDistanceAndVel(beginEndVel, distance, constraints)
            }
            val seg1 = AccelSegment(constraints.maxDeceleration, (-beginEndVel / constraints.maxDeceleration).value)
            val seg2 = AccelSegment(-constraints.maxAcceleration, (-beginEndVel / constraints.maxAcceleration).value)
            val profile = DistanceMotionProfile(State(Distance(), beginEndVel))
            profile += seg1
            profile += generateSimpleProfileNonNegativeDistanceAndVel(
                Distance(),
                State(distance, beginEndVel).invoke(seg2, reversed = true).position
                - State(Distance(), beginEndVel).invoke(seg1).position,
                constraints
            )
            profile += seg2
            return profile
        }

        private fun generateSimpleProfileNonNegativeDistanceAndVel(
            beginEndVel: Distance,
            distance: Distance,
            constraints: Constraints
        ): DistanceMotionProfile {
            /// promise: beginEndVel obeys constraints, distance and beginEndVel are non-negative and in common unit
            val seg1 = AccelSegment(constraints.maxAcceleration, ((constraints.maxVelocity - beginEndVel) / constraints.maxAcceleration).value)
            val seg2 = AccelSegment(-constraints.maxDeceleration, ((constraints.maxVelocity - beginEndVel) / constraints.maxDeceleration).value)
            val state = State(Distance(), beginEndVel).invoke(seg1).invoke(seg2)
            if (state.position <= distance) {
                val profile = DistanceMotionProfile(State(Distance(), beginEndVel))
                profile += seg1
                profile += AccelSegment(Distance(), ((distance - state.position) / constraints.maxVelocity).value)
                profile += seg2
                return profile
            }
            val topVel = (distance * 2.0 *
                    (constraints.maxAcceleration * constraints.maxDeceleration / (constraints.maxAcceleration + constraints.maxDeceleration))
                    + beginEndVel * beginEndVel
            ).sqrt()
            val profile = DistanceMotionProfile(State(Distance(), beginEndVel))
            profile += AccelSegment(constraints.maxAcceleration, ((topVel - beginEndVel) / constraints.maxAcceleration).value)
            profile += AccelSegment(-constraints.maxDeceleration, ((topVel - beginEndVel) / constraints.maxDeceleration).value)
            return profile
        }


        private val debugMode: Boolean = false
        private fun randomUnit(rng: Random): DistanceUnit
        {
            return when(rng.nextInt(0, 5)) {
                0 -> DistanceUnits.METER
                1 -> DistanceUnits.CENTIMETER
                2 -> DistanceUnits.MILLIMETER
                3 -> DistanceUnits.INCH
                else -> DistanceUnits.FOOT
            }
        }
        fun randomTest(bound: Double = 100.0, repeat: Int = 1): Double
        {
            if (!debugMode) {
                throw RuntimeException("You shouldn't run a random test with debug off, dude. Not cool.")
            }
            var maxError = 0.0
            val rng = Random(System.currentTimeMillis())
            for (i in 1..repeat) {
                val beginState = State(
                    Distance(randomUnit(rng), rng.nextDouble(-bound, bound)),
                    Distance(randomUnit(rng), rng.nextDouble(-bound, bound)),
                    Distance(randomUnit(rng), rng.nextDouble(-bound, bound)),
                )
                val endState = State(
                    Distance(randomUnit(rng), rng.nextDouble(-bound, bound)),
                    Distance(randomUnit(rng), rng.nextDouble(-bound, bound)),
                    Distance(randomUnit(rng), rng.nextDouble(-bound, bound)),
                )
                val constraints = Constraints(
                    Distance(randomUnit(rng), rng.nextDouble(1.0, bound)),
                    Distance(randomUnit(rng), rng.nextDouble(1.0, bound)),
                    Distance(randomUnit(rng), rng.nextDouble(1.0, bound)),
                )
                val profile = generateProfile(beginState, endState, constraints)
                maxError = maxError.coerceAtLeast(abs((endState.position - profile.endState.position).intoCommon().value))
                maxError = maxError.coerceAtLeast(abs((endState.velocity - profile.endState.velocity).intoCommon().value))
            }
            return maxError
        }
    }
}