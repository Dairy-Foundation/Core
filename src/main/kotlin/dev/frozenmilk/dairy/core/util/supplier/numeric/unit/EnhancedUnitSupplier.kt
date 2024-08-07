package dev.frozenmilk.dairy.core.util.supplier.numeric.unit

import dev.frozenmilk.dairy.core.util.supplier.logical.Conditional
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedComparableNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedNumericSupplier
import dev.frozenmilk.util.modifier.Modifier
import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import java.util.function.Supplier

class EnhancedUnitSupplier<U: Unit<U>, RU: ReifiedUnit<U, RU>> @JvmOverloads constructor(override val supplier: Supplier<out RU>, override val modifier: Modifier<RU> = Modifier { it }) : EnhancedNumericSupplier<RU>(), EnhancedComparableNumericSupplier<RU, Conditional<RU>> {
	override val zero = supplier.get().run { this - this }
	override var current = supplier.get()
	private var offset = zero
	override var position
		get() = get() - offset
		set(value) {
			offset = current - value
		}
	override val velocity get() = previousPositions.homogenise().getVelocity()
	override val rawVelocity get() = previousPositions.last().getVelocity()
	override val acceleration get() = previousVelocities.homogenise().getVelocity()
	override val rawAcceleration get() = previousVelocities.last().getVelocity()
	override fun findErrorPosition(target: RU) = position.findError(target)
	override fun findErrorVelocity(target: RU) = velocity.findError(target)
	override fun findErrorRawVelocity(target: RU) = rawVelocity.findError(target)
	override fun findErrorAcceleration(target: RU) = acceleration.findError(target)
	override fun findErrorRawAcceleration(target: RU) = rawAcceleration.findError(target)
	override fun <N2> merge(supplier: Supplier<out N2>, merge: (RU, N2) -> RU) = EnhancedUnitSupplier({ merge(get(), supplier.get()) }, modifier)
	override fun applyModifier(modifier: Modifier<RU>) = EnhancedUnitSupplier(supplier) { modifier.modify( this.modifier.modify(it) ) }
	override fun setModifier(modifier: Modifier<RU>) = EnhancedUnitSupplier(supplier, modifier)
	override fun conditionalBindPosition() = Conditional(this::position)
	override fun conditionalBindVelocity() = Conditional(this::velocity)
	override fun conditionalBindVelocityRaw() = Conditional(this::rawVelocity)
	override fun conditionalBindAcceleration() = Conditional(this::acceleration)
	override fun conditionalBindAccelerationRaw() = Conditional(this::rawAcceleration)
}