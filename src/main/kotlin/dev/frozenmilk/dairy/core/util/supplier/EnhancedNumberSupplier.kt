package dev.frozenmilk.dairy.core.util.supplier

import java.util.function.Supplier
import kotlin.math.abs

class EnhancedNumberSupplier<N: Number> (val supplier: Supplier<out N>, val modify: (N) -> N = { x -> x }, val lowerDeadzone: Double = 0.0, val upperDeadzone: Double = 0.0) : Supplier<Double> {
	constructor(supplier: Supplier<N>) : this(supplier, { x -> x })

	private var lastResult = get()
	private var previousTime = System.nanoTime()

	// todo merge this with calcified suppliers for things like error,, that would be good
	var velocity = 0.0
		private set

	private var previousVelocity = 0.0

	var acceleration = 0.0
		private set

	override fun get(): Double {
		val result = modify(supplier.get()).toDouble()
		if (result < 0.0 && result >= lowerDeadzone) return 0.0
		if (result > 0.0 && result <= upperDeadzone) return 0.0
		val currentTime = System.nanoTime()
		val deltaTime = (currentTime - previousTime) / 1e9
		velocity = (result - lastResult) / deltaTime
		acceleration = (velocity - previousVelocity) / deltaTime
		previousTime = currentTime
		previousVelocity = velocity
		lastResult = result
		return result
	}

	fun <N2> merge(supplier: Supplier<out N2>, merge: (N, N2) -> N) = EnhancedNumberSupplier({ merge(this.supplier.get(), supplier.get()) }, this.modify, this.lowerDeadzone, this.upperDeadzone)

	/**
	 * define your own [EnhancedBooleanSupplier] from the velocity of this number supplier
	 */
	fun velocityTrigger(fn: (Double) -> Boolean) = EnhancedBooleanSupplier { fn(velocity) }

	/**
	 * true if [EnhancedNumberSupplier.velocity] is greater than [velocity]
	 */
	fun positiveVelocityTrigger(velocity: Double) = EnhancedBooleanSupplier { this.velocity > velocity }
	/**
	 * true if [EnhancedNumberSupplier.velocity] is less than [velocity]
	 */
	fun negativeVelocityTrigger(velocity: Double) = EnhancedBooleanSupplier { this.velocity < velocity }
	/**
	 * true if [EnhancedNumberSupplier.velocity] is greater in magnitude then [velocity]
	 */
	fun unsignedVelocityTrigger(velocity: Double) = EnhancedBooleanSupplier { abs(this.velocity) > velocity }

	/**
	 * define your own [EnhancedBooleanSupplier] from the acceleration of this number supplier
	 */
	fun accelerationTrigger(fn: (Double) -> Boolean) = EnhancedBooleanSupplier { fn(acceleration) }

	/**
	 * true if [EnhancedNumberSupplier.acceleration] is greater than [acceleration]
	 */
	fun positiveAccelerationTrigger(acceleration: Double) = EnhancedBooleanSupplier { this.acceleration > acceleration }
	/**
	 * true if [EnhancedNumberSupplier.acceleration] is less than [acceleration]
	 */
	fun negativeAccelerationTrigger(acceleration: Double) = EnhancedBooleanSupplier { this.acceleration < acceleration }
	/**
	 * true if [EnhancedNumberSupplier.acceleration] is greater in magnitude than [acceleration]
	 */
	fun unsignedAccelerationTrigger(acceleration: Double) = EnhancedBooleanSupplier { abs(this.acceleration) > acceleration }

	/**
	 * non-mutating
	 */
	fun applyModifier(modify: (N) -> N) = EnhancedNumberSupplier(this.supplier, modify, this.lowerDeadzone, this.upperDeadzone)

	/**
	 * non-mutating
	 */
	fun applyDeadzone(deadzone: Double) = EnhancedNumberSupplier(this.supplier, this.modify, -(deadzone.coerceAtLeast(0.0)), deadzone.coerceAtLeast(0.0))
	/**
	 * non-mutating
	 */
	fun applyDeadzone(lowerDeadzone: Double, upperDeadzone: Double) = EnhancedNumberSupplier(this.supplier, this.modify, lowerDeadzone.coerceAtMost(0.0), upperDeadzone.coerceAtLeast(0.0))
	/**
	 * non-mutating
	 */
	fun applyLowerDeadzone(lowerDeadzone: Double) = EnhancedNumberSupplier(this.supplier, this.modify, lowerDeadzone.coerceAtMost(0.0), this.upperDeadzone)
	/**
	 * non-mutating
	 */
	fun applyUpperDeadzone(upperDeadzone: Double) = EnhancedNumberSupplier(this.supplier, this.modify, this.lowerDeadzone, upperDeadzone.coerceAtLeast(0.0))

	fun conditionalBind(): Conditional<Double> = Conditional(this)
}

fun <N: Number> Supplier<N>.conditionalBind(): Conditional<N> = Conditional(this)
