package dev.frozenmilk.dairy.core.util.supplier.numeric

import dev.frozenmilk.dairy.core.util.supplier.logical.Conditional
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import java.util.function.Supplier

open class EnhancedDoubleSupplier(supplier: Supplier<out Double>, modify: (Double) -> Double = { x -> x }, lowerDeadzone: Double = 0.0, upperDeadzone: Double = 0.0) : EnhancedNumberSupplier<Double>(supplier, modify, lowerDeadzone, upperDeadzone) {
	override val velocity get() = previousPositions.homogenise().getVelocity()
	override val rawVelocity get() = previousPositions.last().getVelocity()
	override val acceleration get() =previousVelocities.homogenise().getVelocity()
	override val rawAcceleration get() =previousVelocities.last().getVelocity()
	final override val zero = 0.0
	override var current: Double = supplier.get()
	private var offset = zero
	override var position: Double
		get() = get() - offset
		set(value) {
			offset = current - value
		}
	override fun findErrorPosition(target: Double) = target - position
	override fun findErrorVelocity(target: Double) = target - velocity
	override fun findErrorRawVelocity(target: Double) = target - rawVelocity
	override fun findErrorAcceleration(target: Double) = target - acceleration
	override fun findErrorRawAcceleration(target: Double) = target - rawAcceleration
	override fun <N2> merge(supplier: Supplier<out N2>, merge: (Double, N2) -> Double) = EnhancedDoubleSupplier({ merge(get(), supplier.get()) }, modify, lowerDeadzone, upperDeadzone)
	override fun applyModifier(modify: (Double) -> Double) = EnhancedDoubleSupplier(supplier, modify, lowerDeadzone, upperDeadzone)
	override fun applyDeadzone(deadzone: Double) = EnhancedDoubleSupplier(supplier, modify, -deadzone.coerceAtLeast(0.0), deadzone.coerceAtLeast(0.0))
	override fun applyDeadzone(lowerDeadzone: Double, upperDeadzone: Double) = EnhancedDoubleSupplier(supplier, modify, lowerDeadzone, upperDeadzone)
	override fun applyLowerDeadzone(lowerDeadzone: Double) = EnhancedDoubleSupplier(supplier, modify, lowerDeadzone, upperDeadzone)
	override fun applyUpperDeadzone(upperDeadzone: Double) = EnhancedDoubleSupplier(supplier, modify, lowerDeadzone, upperDeadzone)
	override fun conditionalBindPosition() = Conditional(this::position)
	override fun conditionalBindVelocity() = Conditional(this::velocity)
	override fun conditionalBindVelocityRaw() = Conditional(this::rawVelocity)
	override fun conditionalBindAcceleration() = Conditional(this::acceleration)
	override fun conditionalBindAccelerationRaw() = Conditional(this::rawAcceleration)
}