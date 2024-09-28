package dev.frozenmilk.dairy.core.util.profile.implementation

import dev.frozenmilk.dairy.core.util.profile.MotionProfile
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt
import kotlin.random.Random

class DoubleMotionProfile(beginState: State) : MotionProfile<Double> {
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
        override val position: Double,
        override val velocity: Double,
        override val acceleration: Double = 0.0
    ) : MotionProfile.State<Double> {
        init {
            if (position.isNaN()) {
                throw IllegalArgumentException("position of DoubleMotionProfile.State must not be NaN.")
            }
            if (velocity.isNaN()) {
                throw IllegalArgumentException("velocity of DoubleMotionProfile.State must not be NaN.")
            }
            if (acceleration.isNaN()) {
                throw IllegalArgumentException("acceleration of DoubleMotionProfile.State must not be NaN.")
            }
        }

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

        fun obeys(constraints: Constraints): Boolean = (abs(velocity) <= constraints.maxVelocity)

        operator fun invoke(accelSegment: AccelSegment, reversed: Boolean = false): State {
            val multi = if (reversed) -1.0 else 1.0
            return State(
                position + velocity * accelSegment.time * multi + accelSegment.acceleration * accelSegment.time * accelSegment.time / 2.0,
                velocity + accelSegment.acceleration * accelSegment.time * multi,
                accelSegment.acceleration
            )
        }
        operator fun invoke(profile: DoubleMotionProfile, reversed: Boolean = false): State {
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
        val maxVelocity: Double,
        val maxAcceleration: Double,
        val maxDeceleration: Double
    ) {
        init {
            if (maxVelocity.isNaN() || maxVelocity <= 0.0) {
                throw IllegalArgumentException("maxVelocity of DoubleMotionProfile.Constraints must be positive.")
            }
            if (maxAcceleration.isNaN() || maxAcceleration <= 0.0) {
                throw IllegalArgumentException("maxAcceleration of DoubleMotionProfile.Constraints must be positive.")
            }
            if (maxDeceleration.isNaN() || maxDeceleration <= 0.0) {
                throw IllegalArgumentException("maxDeceleration of DoubleMotionProfile.Constraints must be positive.")
            }
        }
    }

    class AccelSegment(
        val acceleration: Double,
        val time: Double
    ) {
        constructor() : this(0.0, 0.0)
        init {
            if (acceleration.isNaN()) {
                throw IllegalArgumentException("acceleration of DoubleMotionProfile.AccelSegment must not be NaN.")
            }
            if (time.isNaN() || (debugMode && time < 0.0)) {
                throw IllegalArgumentException("time of DoubleMotionProfile.AccelSegment must be non-negative.")
            }
        }

        operator fun unaryPlus() = this
        operator fun unaryMinus() = AccelSegment(
            -acceleration, time
        )
    }


    private operator fun unaryPlus() = this
    private operator fun unaryMinus(): DoubleMotionProfile {
        val profile = DoubleMotionProfile(-beginState)
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

    private operator fun plusAssign(profile: DoubleMotionProfile) {
        for (segment in profile.accelSegments) {
            plusAssign(segment)
        }
    }
    private operator fun minusAssign(profile: DoubleMotionProfile) {
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
            endVel: Double,
            constraints: Constraints
        ): DoubleMotionProfile {
            return (if (beginState.velocity >= 0.0)
                generateVelProfileNonNegativeBeginVel(beginState, endVel, constraints)
            else -generateVelProfileNonNegativeBeginVel(-beginState, -endVel, constraints)
                    ).also {
                if (!debugMode) it.endState = State(it.endState.position, endVel)
            }
        }

        fun generateVelProfile(
            beginVel: Double,
            endVel: Double,
            constraints: Constraints
        ) = generateVelProfile(State(0.0, beginVel), endVel, constraints)

        private fun generateVelProfileNonNegativeBeginVel(
            beginState: State,
            endVel: Double,
            constraints: Constraints
        ): DoubleMotionProfile {
            /// promise: beginState.velocity is non-negative
            val profile = DoubleMotionProfile(beginState)
            if (beginState.velocity == endVel) return profile
            if (beginState.velocity < endVel) {
                profile += AccelSegment(constraints.maxAcceleration, (endVel - beginState.velocity) / constraints.maxAcceleration)
                return profile
            }
            if (endVel >= 0.0) {
                profile += AccelSegment(-constraints.maxDeceleration, (beginState.velocity - endVel) / constraints.maxDeceleration)
                return profile
            }
            profile += AccelSegment(-constraints.maxDeceleration, beginState.velocity / constraints.maxDeceleration)
            profile += AccelSegment(-constraints.maxAcceleration, -endVel / constraints.maxAcceleration)
            return profile
        }


        fun generateProfile(
            beginState: State,
            endState: State,
            constraints: Constraints
        ): DoubleMotionProfile {
            val seg1 = if (beginState.obeys(constraints)) AccelSegment() else AccelSegment(
                -constraints.maxDeceleration * sign(beginState.velocity),
                (abs(beginState.velocity) - constraints.maxVelocity) / constraints.maxDeceleration
            )
            val seg2 = if (endState.obeys(constraints)) AccelSegment() else AccelSegment(
                constraints.maxAcceleration * sign(endState.velocity),
                (abs(endState.velocity) - constraints.maxVelocity) / constraints.maxAcceleration
            )
            val profile = DoubleMotionProfile(beginState)
            profile += seg1
            profile += generateProfileObeying(
                beginState(seg1).let { State(it.position, it.velocity.coerceIn(-constraints.maxVelocity, constraints.maxVelocity)) },
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
        ): DoubleMotionProfile {
            /// promise: beginState and endState are within constraints
            val velProfileBeginEnd = generateVelProfile(beginState.velocity, endState.velocity, constraints)
            val velProfileBeginZero = generateVelProfile(beginState.velocity, 0.0, constraints)
            val velProfileZeroEnd = generateVelProfile(0.0, endState.velocity, constraints)

            val profileBegin = DoubleMotionProfile(beginState)
            profileBegin += generateSimpleProfile(
                beginState.velocity,
                endState(velProfileBeginEnd, reversed = true).position - beginState.position,
                constraints
            )
            profileBegin += velProfileBeginEnd

            val profileEnd = DoubleMotionProfile(beginState)
            profileEnd += velProfileBeginEnd
            profileEnd += generateSimpleProfile(
                endState.velocity,
                endState.position - beginState(velProfileBeginEnd).position,
                constraints
            )

            val profileZero = DoubleMotionProfile(beginState)
            profileZero += velProfileBeginZero
            profileZero += generateSimpleProfile(
                0.0,
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
            beginEndVel: Double,
            distance: Double,
            constraints: Constraints
        ): DoubleMotionProfile {
            /// promise: beginEndVel obeys constraints
            return if (distance >= 0.0)
                generateSimpleProfileNonNegativeDistance(beginEndVel, distance, constraints)
            else -generateSimpleProfileNonNegativeDistance(-beginEndVel, -distance, constraints)
        }

        private fun generateSimpleProfileNonNegativeDistance(
            beginEndVel: Double,
            distance: Double,
            constraints: Constraints
        ): DoubleMotionProfile {
            /// promise: beginEndVel obeys constraints, distance is non-negative
            if (distance == 0.0) return DoubleMotionProfile(State(0.0, beginEndVel))
            if (beginEndVel >= 0.0) {
                return generateSimpleProfileNonNegativeDistanceAndVel(beginEndVel, distance, constraints)
            }
            val seg1 = AccelSegment(constraints.maxDeceleration, -beginEndVel / constraints.maxDeceleration)
            val seg2 = AccelSegment(-constraints.maxAcceleration, -beginEndVel / constraints.maxAcceleration)
            val profile = DoubleMotionProfile(State(0.0, beginEndVel))
            profile += seg1
            profile += generateSimpleProfileNonNegativeDistanceAndVel(
                0.0,
                State(distance, beginEndVel).invoke(seg2, reversed = true).position
                - State(0.0, beginEndVel).invoke(seg1).position,
                constraints
            )
            profile += seg2
            return profile
        }

        private fun generateSimpleProfileNonNegativeDistanceAndVel(
            beginEndVel: Double,
            distance: Double,
            constraints: Constraints
        ): DoubleMotionProfile {
            /// promise: beginEndVel obeys constraints, distance and beginEndVel are non-negative
            val seg1 = AccelSegment(constraints.maxAcceleration, (constraints.maxVelocity - beginEndVel) / constraints.maxAcceleration)
            val seg2 = AccelSegment(-constraints.maxDeceleration, (constraints.maxVelocity - beginEndVel) / constraints.maxDeceleration)
            val state = State(0.0, beginEndVel).invoke(seg1).invoke(seg2)
            if (state.position <= distance) {
                val profile = DoubleMotionProfile(State(0.0, beginEndVel))
                profile += seg1
                profile += AccelSegment(0.0, (distance - state.position) / constraints.maxVelocity)
                profile += seg2
                return profile
            }
            val topVel = sqrt(distance * 2.0 *
                    (constraints.maxAcceleration * constraints.maxDeceleration / (constraints.maxAcceleration + constraints.maxDeceleration))
                    + beginEndVel * beginEndVel
            )
            val profile = DoubleMotionProfile(State(0.0, beginEndVel))
            profile += AccelSegment(constraints.maxAcceleration, (topVel - beginEndVel) / constraints.maxAcceleration)
            profile += AccelSegment(-constraints.maxDeceleration, (topVel - beginEndVel) / constraints.maxDeceleration)
            return profile
        }


        private val debugMode: Boolean = false
        fun randomTest(bound: Double = 100.0, repeat: Int = 1): Double
        {
            if (!debugMode) {
                throw RuntimeException("You shouldn't run a random test with debug off, dude. Not cool.")
            }
            var maxError = 0.0
            val rng = Random(System.currentTimeMillis())
            for (i in 1..repeat) {
                val beginState = State(
                    rng.nextDouble(-bound, bound),
                    rng.nextDouble(-bound, bound),
                    rng.nextDouble(-bound, bound),
                )
                val endState = State(
                    rng.nextDouble(-bound, bound),
                    rng.nextDouble(-bound, bound),
                    rng.nextDouble(-bound, bound),
                )
                val constraints = Constraints(
                    rng.nextDouble(1.0, bound),
                    rng.nextDouble(1.0, bound),
                    rng.nextDouble(1.0, bound),
                )
                val profile = generateProfile(beginState, endState, constraints)
                maxError = maxError.coerceAtLeast(abs(endState.position - profile.endState.position))
                maxError = maxError.coerceAtLeast(abs(endState.velocity - profile.endState.velocity))
            }
            return maxError
        }
    }
}