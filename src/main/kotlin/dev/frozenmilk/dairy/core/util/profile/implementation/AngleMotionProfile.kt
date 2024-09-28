package dev.frozenmilk.dairy.core.util.profile.implementation

import dev.frozenmilk.dairy.core.util.profile.MotionProfile
import dev.frozenmilk.util.units.angle.Angle
import dev.frozenmilk.util.units.angle.AngleUnit
import dev.frozenmilk.util.units.angle.AngleUnits
import dev.frozenmilk.util.units.angle.Wrapping
import dev.frozenmilk.util.units.distance.Distance
import dev.frozenmilk.util.units.distance.DistanceUnit
import dev.frozenmilk.util.units.distance.DistanceUnits
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

class AngleMotionProfile(beginState: State) : MotionProfile<Angle> {
    val beginState: State
    private val accelSegments = ArrayList<AccelSegment>()
    var endState: State
        private set
    var totalDuration: Double = 0.0
        private set
    val startTimestamp: Long = System.nanoTime()

    init {
        this.beginState = State(beginState.position, beginState.velocity)
        this.endState = this.beginState
    }

    class State(
        position: Angle,
        velocity: Angle,
        acceleration: Angle = Angle(wrapping = Wrapping.LINEAR)
    ) : MotionProfile.State<Angle> {
        init {
            if (position.isNaN()) {
                throw IllegalArgumentException("position of AngleMotionProfile.State must not be NaN.")
            }
            if (velocity.isNaN() || velocity.wrapping != Wrapping.LINEAR) {
                throw IllegalArgumentException("velocity of AngleMotionProfile.State must not be NaN and must be Linear.")
            }
            if (acceleration.isNaN() || acceleration.wrapping != Wrapping.LINEAR) {
                throw IllegalArgumentException("acceleration of AngleMotionProfile.State must not be NaN and must be Linear.")
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
                (position + velocity * accelSegment.time * multi + accelSegment.acceleration * accelSegment.time * accelSegment.time / 2.0).into(position.wrapping),
                velocity + accelSegment.acceleration * accelSegment.time * multi,
                accelSegment.acceleration
            )
        }
        operator fun invoke(profile: AngleMotionProfile, reversed: Boolean = false): State {
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
        maxVelocity: Angle,
        maxAcceleration: Angle,
        maxDeceleration: Angle
    ) {
        init {
            if (maxVelocity.isNaN() || maxVelocity.wrapping != Wrapping.LINEAR || maxVelocity <= Angle(wrapping = Wrapping.LINEAR)) {
                throw IllegalArgumentException("maxVelocity of AngleMotionProfile.Constraints must be positive and Linear.")
            }
            if (maxAcceleration.isNaN() || maxAcceleration.wrapping != Wrapping.LINEAR || maxAcceleration <= Angle(wrapping = Wrapping.LINEAR)) {
                throw IllegalArgumentException("maxAcceleration of AngleMotionProfile.Constraints must be positive and Linear.")
            }
            if (maxDeceleration.isNaN() || maxDeceleration.wrapping != Wrapping.LINEAR || maxDeceleration <= Angle(wrapping = Wrapping.LINEAR)) {
                throw IllegalArgumentException("maxDeceleration of AngleMotionProfile.Constraints must be positive and Linear.")
            }
        }

        val maxVelocity = maxVelocity.intoCommon()
        val maxAcceleration = maxAcceleration.intoCommon()
        val maxDeceleration = maxDeceleration.intoCommon()
    }

    class AccelSegment(
        acceleration: Angle,
        val time: Double
    ) {
        constructor() : this(Angle(wrapping = Wrapping.LINEAR), 0.0)
        init {
            if (acceleration.isNaN() || acceleration.wrapping != Wrapping.LINEAR) {
                throw IllegalArgumentException("acceleration of AngleMotionProfile.AccelSegment must not be NaN and must be Linear.")
            }
            if (time.isNaN() || (debugMode && time < 0.0)) {
                throw IllegalArgumentException("time of AngleMotionProfile.AccelSegment must be non-negative.")
            }
        }

        val acceleration = acceleration.intoCommon()

        operator fun unaryPlus() = this
        operator fun unaryMinus() = AccelSegment(
            -acceleration, time
        )
    }


    private operator fun unaryPlus() = this
    private operator fun unaryMinus(): AngleMotionProfile {
        val profile = AngleMotionProfile(-beginState)
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

    private operator fun plusAssign(profile: AngleMotionProfile) {
        for (segment in profile.accelSegments) {
            plusAssign(segment)
        }
    }
    private operator fun minusAssign(profile: AngleMotionProfile) {
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

    override fun get(t: Double): State {
        if (t <= 0.0) {
            return beginState
        }
        if (t >= totalDuration) {
            return endState
        }
        return getInBounds(t)
    }
    override fun get() = get((System.nanoTime() - startTimestamp).toDouble() / 1e9)

    override fun getEndless(t: Double): State {
        if (t <= 0.0) {
            return State(beginState.position + beginState.velocity * t, beginState.velocity)
        }
        if (t >= totalDuration) {
            return State(endState.position + endState.velocity * (t - totalDuration), endState.velocity)
        }
        return getInBounds(t)
    }
    override fun getEndless() = getEndless((System.nanoTime() - startTimestamp).toDouble() / 1e9)


    companion object {
        fun generateVelProfile(
            beginState: State,
            endVel: Angle,
            constraints: Constraints
        ): AngleMotionProfile {
            if (endVel.wrapping != Wrapping.LINEAR) {
                throw IllegalArgumentException("endVel of AngleMotionProfile.generateVelProfile must be Linear.")
            }
            return (if (beginState.velocity >= Angle(wrapping = Wrapping.LINEAR))
                generateVelProfileNonNegativeBeginVel(beginState, endVel.intoCommon(), constraints)
            else -generateVelProfileNonNegativeBeginVel(-beginState, -endVel.intoCommon(), constraints)
                    ).also {
                if (!debugMode) it.endState = State(it.endState.position, endVel)
            }
        }

        fun generateVelProfile(
            beginVel: Angle,
            endVel: Angle,
            constraints: Constraints
        ) = generateVelProfile(State(Angle(wrapping = Wrapping.LINEAR), beginVel), endVel, constraints)

        private fun generateVelProfileNonNegativeBeginVel(
            beginState: State,
            endVel: Angle,
            constraints: Constraints
        ): AngleMotionProfile {
            /// promise: beginState.velocity is non-negative, endVel is linear and in common unit
            val profile = AngleMotionProfile(beginState)
            if (beginState.velocity == endVel) return profile
            if (beginState.velocity < endVel) {
                profile += AccelSegment(constraints.maxAcceleration, ((endVel - beginState.velocity) / constraints.maxAcceleration).value)
                return profile
            }
            if (endVel >= Angle(wrapping = Wrapping.LINEAR)) {
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
            constraints: Constraints
        ): AngleMotionProfile {
            // note: i force the wrapping mode of beginState.position to be the same as for endState.position
            val seg1 = if (beginState.obeys(constraints)) AccelSegment() else AccelSegment(
                -constraints.maxDeceleration * beginState.velocity.sign,
                ((beginState.velocity.absoluteValue - constraints.maxVelocity) / constraints.maxDeceleration).value
            )
            val seg2 = if (endState.obeys(constraints)) AccelSegment() else AccelSegment(
                constraints.maxAcceleration * endState.velocity.sign,
                ((endState.velocity.absoluteValue - constraints.maxVelocity) / constraints.maxAcceleration).value
            )
            val profile = AngleMotionProfile(beginState.let { State(it.position.into(endState.position.wrapping), it.velocity) })
            profile += seg1
            profile += generateProfileObeying(
                beginState(seg1).let { State(it.position.into(endState.position.wrapping), it.velocity.coerceIn(-constraints.maxVelocity, constraints.maxVelocity)) },
                endState(seg2, reversed = true).let { State(it.position, it.velocity.coerceIn(-constraints.maxVelocity, constraints.maxVelocity)) },
                constraints
            )
            profile += seg2
            if (!debugMode) profile.endState = State(endState.position, endState.velocity)
            return profile
        }

        private fun generateProfileObeying(
            beginState: State,
            endState: State,
            constraints: Constraints
        ): AngleMotionProfile {
            /// promise: beginState and endState are within constraints, begin and end pos are in the same wrapping mode
            val velProfileBeginEnd = generateVelProfile(beginState.velocity, endState.velocity, constraints)
            val velProfileBeginZero = generateVelProfile(beginState.velocity, Angle(wrapping = Wrapping.LINEAR), constraints)
            val velProfileZeroEnd = generateVelProfile(Angle(wrapping = Wrapping.LINEAR), endState.velocity, constraints)

            val profileBegin = AngleMotionProfile(beginState)
            profileBegin += generateSimpleProfile(
                beginState.velocity,
                beginState.position.findError(endState(velProfileBeginEnd, reversed = true).position),
                constraints
            )
            profileBegin += velProfileBeginEnd

            val profileEnd = AngleMotionProfile(beginState)
            profileEnd += velProfileBeginEnd
            profileEnd += generateSimpleProfile(
                endState.velocity,
                beginState(velProfileBeginEnd).position.findError(endState.position),
                constraints
            )

            val profileZero = AngleMotionProfile(beginState)
            profileZero += velProfileBeginZero
            profileZero += generateSimpleProfile(
                Angle(wrapping = Wrapping.LINEAR),
                beginState(velProfileBeginZero).position.findError(endState(velProfileZeroEnd, reversed = true).position),
                constraints
            )
            profileZero += velProfileZeroEnd

            var bestProfile = profileBegin
            if (profileEnd.totalDuration < bestProfile.totalDuration) bestProfile = profileEnd
            if (profileZero.totalDuration < bestProfile.totalDuration) bestProfile = profileZero
            return bestProfile
        }


        private fun generateSimpleProfile(
            beginEndVel: Angle,
            distance: Angle,
            constraints: Constraints
        ): AngleMotionProfile {
            /// promise: beginEndVel is linear and obeys constraints, beginEndVel and distance are in common unit
            if (distance.wrapping == Wrapping.LINEAR) {
                return generateSimpleLinearProfile(beginEndVel, distance, constraints)
            }
            val profile1 = generateSimpleLinearProfile(
                beginEndVel,
                Angle(AngleUnits.RADIAN, Wrapping.LINEAR, distance.intoWrapping().value),
                constraints
            )
            val profile2 = generateSimpleLinearProfile(
                beginEndVel,
                Angle(AngleUnits.RADIAN, Wrapping.LINEAR, distance.intoWrapping().value - Math.PI * 2.0),
                constraints
            )
            return if (profile1.totalDuration <= profile2.totalDuration) profile1 else profile2
        }

        private fun generateSimpleLinearProfile(
            beginEndVel: Angle,
            distance: Angle,
            constraints: Constraints
        ): AngleMotionProfile {
            /// promise: beginEndVel and distance are linear and in common unit, beginEndVel obeys constraints
            return if (distance >= Angle(wrapping = Wrapping.LINEAR))
                generateSimpleLinearProfileNonNegativeDistance(beginEndVel, distance, constraints)
            else -generateSimpleLinearProfileNonNegativeDistance(-beginEndVel, -distance, constraints)
        }

        private fun generateSimpleLinearProfileNonNegativeDistance(
            beginEndVel: Angle,
            distance: Angle,
            constraints: Constraints
        ): AngleMotionProfile {
            /// promise: beginEndVel and distance are linear and in common unit, beginEndVel obeys constraints, distance is non-negative
            if (distance == Angle(wrapping = Wrapping.LINEAR)) return AngleMotionProfile(State(Angle(wrapping = Wrapping.LINEAR), beginEndVel))
            if (beginEndVel >= Angle(wrapping = Wrapping.LINEAR)) {
                return generateSimpleLinearProfileNonNegativeDistanceAndVel(beginEndVel, distance, constraints)
            }
            val seg1 = AccelSegment(constraints.maxDeceleration, (-beginEndVel / constraints.maxDeceleration).value)
            val seg2 = AccelSegment(-constraints.maxAcceleration, (-beginEndVel / constraints.maxAcceleration).value)
            val profile = AngleMotionProfile(State(Angle(wrapping = Wrapping.LINEAR), beginEndVel))
            profile += seg1
            profile += generateSimpleLinearProfileNonNegativeDistanceAndVel(
                Angle(wrapping = Wrapping.LINEAR),
                State(distance, beginEndVel).invoke(seg2, reversed = true).position
                - State(Angle(wrapping = Wrapping.LINEAR), beginEndVel).invoke(seg1).position,
                constraints
            )
            profile += seg2
            return profile
        }

        private fun generateSimpleLinearProfileNonNegativeDistanceAndVel(
            beginEndVel: Angle,
            distance: Angle,
            constraints: Constraints
        ): AngleMotionProfile {
            /// promise: beginEndVel and distance are linear, in common unit and non-negative, beginEndVel obeys constraints
            val seg1 = AccelSegment(constraints.maxAcceleration, ((constraints.maxVelocity - beginEndVel) / constraints.maxAcceleration).value)
            val seg2 = AccelSegment(-constraints.maxDeceleration, ((constraints.maxVelocity - beginEndVel) / constraints.maxDeceleration).value)
            val state = State(Angle(wrapping = Wrapping.LINEAR), beginEndVel).invoke(seg1).invoke(seg2)
            if (state.position <= distance) {
                val profile = AngleMotionProfile(State(Angle(wrapping = Wrapping.LINEAR), beginEndVel))
                profile += seg1
                profile += AccelSegment(Angle(wrapping = Wrapping.LINEAR), ((distance - state.position) / constraints.maxVelocity).value)
                profile += seg2
                return profile
            }
            val topVel = (distance * 2.0 *
                    (constraints.maxAcceleration * constraints.maxDeceleration / (constraints.maxAcceleration + constraints.maxDeceleration))
                    + beginEndVel * beginEndVel
            ).sqrt()
            val profile = AngleMotionProfile(State(Angle(wrapping = Wrapping.LINEAR), beginEndVel))
            profile += AccelSegment(constraints.maxAcceleration, ((topVel - beginEndVel) / constraints.maxAcceleration).value)
            profile += AccelSegment(-constraints.maxDeceleration, ((topVel - beginEndVel) / constraints.maxDeceleration).value)
            return profile
        }


        private val debugMode: Boolean = false
        private fun randomUnit(rng: Random): AngleUnit
        {
            return when(rng.nextInt(0, 2)) {
                0 -> AngleUnits.RADIAN
                else -> AngleUnits.DEGREE
            }
        }
        private fun randomWrapping(rng: Random): Wrapping
        {
            return when(rng.nextInt(0, 3)) {
                0 -> Wrapping.WRAPPING
                1 -> Wrapping.RELATIVE
                else -> Wrapping.LINEAR
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
                    Angle(randomUnit(rng), randomWrapping(rng), rng.nextDouble(-bound, bound)),
                    Angle(randomUnit(rng), Wrapping.LINEAR, rng.nextDouble(-bound, bound)),
                    Angle(randomUnit(rng), Wrapping.LINEAR, rng.nextDouble(-bound, bound)),
                )
                val endState = State(
                    Angle(randomUnit(rng), randomWrapping(rng), rng.nextDouble(-bound, bound)),
                    Angle(randomUnit(rng), Wrapping.LINEAR, rng.nextDouble(-bound, bound)),
                    Angle(randomUnit(rng), Wrapping.LINEAR, rng.nextDouble(-bound, bound)),
                )
                val constraints = Constraints(
                    Angle(randomUnit(rng), Wrapping.LINEAR, rng.nextDouble(1.0, bound)),
                    Angle(randomUnit(rng), Wrapping.LINEAR, rng.nextDouble(1.0, bound)),
                    Angle(randomUnit(rng), Wrapping.LINEAR, rng.nextDouble(1.0, bound)),
                )
                val profile = generateProfile(beginState, endState, constraints)
                maxError = maxError.coerceAtLeast(abs((profile.endState.position.findError(endState.position)).intoCommon().value))
                maxError = maxError.coerceAtLeast(abs((profile.endState.velocity.findError(endState.velocity)).intoCommon().value))
            }
            return maxError
        }
    }
}