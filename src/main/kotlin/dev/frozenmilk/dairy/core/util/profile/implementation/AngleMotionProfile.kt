package dev.frozenmilk.dairy.core.util.profile.implementation

import dev.frozenmilk.dairy.core.util.profile.MotionProfile
import dev.frozenmilk.util.units.angle.Angle
import dev.frozenmilk.util.units.angle.AngleUnits
import dev.frozenmilk.util.units.angle.Wrapping
import dev.frozenmilk.util.units.distance.Distance
import kotlin.math.sqrt

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
            if (time.isNaN() /* || time < 0.0 */) {
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
                it.endState = State(it.endState.position, endVel)
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
            val seg1 = if (beginState.obeys(constraints)) AccelSegment() else AccelSegment(
                -constraints.maxDeceleration * beginState.velocity.sign,
                ((beginState.velocity.absoluteValue - constraints.maxVelocity) / constraints.maxDeceleration).value
            )
            val seg2 = if (endState.obeys(constraints)) AccelSegment() else AccelSegment(
                constraints.maxAcceleration * endState.velocity.sign,
                ((endState.velocity.absoluteValue - constraints.maxVelocity) / constraints.maxAcceleration).value
            )
            val profile = AngleMotionProfile(beginState)
            profile += seg1
            profile += generateProfileObeying(
                beginState(seg1).let { State(it.position, it.velocity.coerceIn(-constraints.maxVelocity, constraints.maxVelocity)) },
                endState(seg2, reversed = true).let { State(it.position, it.velocity.coerceIn(-constraints.maxVelocity, constraints.maxVelocity)) },
                constraints
            )
            profile += seg2
            profile.endState = State(endState.position, endState.velocity)
            return profile
        }

        private fun generateProfileObeying(
            beginState: State,
            endState: State,
            constraints: Constraints
        ): AngleMotionProfile {
            /// promise: beginState and endState are within constraints
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
            /// promise: beginEndVel is linear and obeys constraints
            if (distance.wrapping == Wrapping.LINEAR) {
                return generateSimpleLinearProfile(beginEndVel, distance, constraints)
            }
            val profile1 = generateSimpleLinearProfile(beginEndVel, Angle(unit = AngleUnits.RADIAN, wrapping = Wrapping.LINEAR, value = ))
        }

        private fun generateSimpleLinearProfile(
            beginEndVel: Angle,
            distance: Angle,
            constraints: Constraints
        ): AngleMotionProfile {
            /// promise: beginEndVel is linear and obeys constraints, distance is linear
            return if (distance >= Distance())
                generateSimpleLinearProfileNonNegativeDistance(beginEndVel, distance, constraints)
            else -generateSimpleLinearProfileNonNegativeDistance(-beginEndVel, -distance, constraints)
        }

        private fun generateSimpleLinearProfileNonNegativeDistance(
            beginEndVel: Distance,
            distance: Distance,
            constraints: Constraints
        ): AngleMotionProfile {
            /// promise: beginEndVel obeys constraints, distance is non-negative
            if (distance == Distance()) return AngleMotionProfile(State(Distance(), beginEndVel))
            if (beginEndVel >= Distance()) {
                return generateSimpleLinearProfileNonNegativeDistanceAndVel(beginEndVel, distance, constraints)
            }
            val seg1 = AccelSegment(constraints.maxDeceleration, (-beginEndVel / constraints.maxDeceleration).value)
            val seg2 = AccelSegment(-constraints.maxAcceleration, (-beginEndVel / constraints.maxAcceleration).value)
            val profile = AngleMotionProfile(State(Distance(), beginEndVel))
            profile += seg1
            profile += generateSimpleLinearProfileNonNegativeDistanceAndVel(
                Distance(),
                State(distance, beginEndVel).invoke(seg2, reversed = true).position
                - State(Distance(), beginEndVel).invoke(seg1).position,
                constraints
            )
            profile += seg2
            return profile
        }

        private fun generateSimpleLinearProfileNonNegativeDistanceAndVel(
            beginEndVel: Distance,
            distance: Distance,
            constraints: Constraints
        ): AngleMotionProfile {
            /// promise: beginEndVel obeys constraints, distance and beginEndVel are non-negative
            val seg1 = AccelSegment(constraints.maxAcceleration, ((constraints.maxVelocity - beginEndVel) / constraints.maxAcceleration).value)
            val seg2 = AccelSegment(-constraints.maxDeceleration, ((constraints.maxVelocity - beginEndVel) / constraints.maxDeceleration).value)
            val state = State(Distance(), beginEndVel).invoke(seg1).invoke(seg2)
            if (state.position <= distance) {
                val profile = AngleMotionProfile(State(Distance(), beginEndVel))
                profile += seg1
                profile += AccelSegment(Distance(), ((distance - state.position) / constraints.maxVelocity).value)
                profile += seg2
                return profile
            }
            val topVel = (distance * 2.0 *
                    (constraints.maxAcceleration * constraints.maxDeceleration / (constraints.maxAcceleration + constraints.maxDeceleration))
                    + beginEndVel * beginEndVel
            ).sqrt()
            val profile = AngleMotionProfile(State(Distance(), beginEndVel))
            profile += AccelSegment(constraints.maxAcceleration, ((topVel - beginEndVel) / constraints.maxAcceleration).value)
            profile += AccelSegment(-constraints.maxDeceleration, ((topVel - beginEndVel) / constraints.maxDeceleration).value)
            return profile
        }
    }
}