package dev.frozenmilk.dairy.core.util.supplier.numeric

import dev.frozenmilk.dairy.core.util.supplier.logical.Conditional
import dev.frozenmilk.util.modifier.Modifier
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import java.util.function.Supplier

class EnhancedDoubleSupplier @JvmOverloads constructor(override val supplier: Supplier<out Double>, override val modifier: Modifier<Double> = Modifier { it }) : EnhancedNumericSupplier<Double>(), EnhancedComparableNumericSupplier<Double, Conditional<Double>> {
	override val velocity get() = previousPositions.homogenise().getVelocity()
	override val rawVelocity get() = previousPositions.last().getVelocity()
	override val acceleration get() = previousVelocities.homogenise().getVelocity()
	override val rawAcceleration get() = previousVelocities.last().getVelocity()
	override val zero = 0.0
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
	override fun <N2> merge(supplier: Supplier<out N2>, merge: (Double, N2) -> Double) = EnhancedDoubleSupplier({ merge(get(), supplier.get()) }, modifier)
	override fun applyModifier(modifier: Modifier<Double>) = EnhancedDoubleSupplier(supplier) { modifier.modify(this.modifier.modify(it)) }
	override fun setModifier(modifier: Modifier<Double>) = EnhancedDoubleSupplier(supplier, modifier)
	override fun conditionalBindPosition() = Conditional(this::position)
	override fun conditionalBindVelocity() = Conditional(this::velocity)
	override fun conditionalBindVelocityRaw() = Conditional(this::rawVelocity)
	override fun conditionalBindAcceleration() = Conditional(this::acceleration)
	override fun conditionalBindAccelerationRaw() = Conditional(this::rawAcceleration)
}