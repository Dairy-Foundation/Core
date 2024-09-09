package dev.frozenmilk.dairy.core.util.supplier.numeric.unit

import dev.frozenmilk.dairy.core.util.supplier.logical.Conditional
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedComparableNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedNumericSupplier
import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import java.util.function.Supplier

@Suppress("INAPPLICABLE_JVM_NAME")
class EnhancedUnitSupplier<RU: ReifiedUnit<*, RU>> (override val supplier: Supplier<out RU>) : EnhancedNumericSupplier<RU>(), EnhancedComparableNumericSupplier<RU, Conditional<RU>> {
	override val zero = supplier.get().run { this - this }
	override var currentState = supplier.get()
	private var offset = zero
	@get:JvmName("state")
	@set:JvmName("state")
	override var state
		get() = get() - offset
		set(value) {
			offset = currentState - value
		}
	@get:JvmName("velocity")
	override val velocity get() = previousPositions.homogenise().getVelocity()
	@get:JvmName("rawVelocity")
	override val rawVelocity get() = previousPositions.last().getVelocity()
	@get:JvmName("acceleration")
	override val acceleration get() = previousVelocities.homogenise().getVelocity()
	@get:JvmName("rawAcceleration")
	override val rawAcceleration get() = previousVelocities.last().getVelocity()
	override fun conditionalBindState() = Conditional(this::state)
	override fun conditionalBindVelocity() = Conditional(this::velocity)
	override fun conditionalBindVelocityRaw() = Conditional(this::rawVelocity)
	override fun conditionalBindAcceleration() = Conditional(this::acceleration)
	override fun conditionalBindAccelerationRaw() = Conditional(this::rawAcceleration)
}