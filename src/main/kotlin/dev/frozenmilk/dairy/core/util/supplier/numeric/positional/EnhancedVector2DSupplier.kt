package dev.frozenmilk.dairy.core.util.supplier.numeric.positional

import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedNumericSupplier
import dev.frozenmilk.util.modifier.Modifier
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import dev.frozenmilk.util.units.position.Vector2D
import java.util.function.Supplier

class EnhancedVector2DSupplier(override val supplier: Supplier<out Vector2D>, override val modifier: Modifier<Vector2D> = Modifier { it }) : EnhancedNumericSupplier<Vector2D>() {
	override val zero = Vector2D()
	private var offset = zero
	override var current = supplier.get()
	override var position
		get() = get() - offset
		set(value) {
			offset = current - value
		}
	override val velocity get() = previousPositions.homogenise().getVelocity()
	override val rawVelocity get() = previousPositions.last().getVelocity()
	override val acceleration get() = previousVelocities.homogenise().getVelocity()
	override val rawAcceleration get() = previousVelocities.last().getVelocity()
	override fun applyModifier(modifier: Modifier<Vector2D>) = EnhancedVector2DSupplier(supplier) { modifier.modify(this.modifier.modify(it)) }
	override fun setModifier(modifier: Modifier<Vector2D>) = EnhancedVector2DSupplier(supplier, modifier)
	override fun <N2> merge(supplier: Supplier<out N2>, merge: (Vector2D, N2) -> Vector2D) = EnhancedVector2DSupplier({ merge(get(), supplier.get()) }, modifier)
	override fun findErrorPosition(target: Vector2D) = target - position
	override fun findErrorVelocity(target: Vector2D) = target - velocity
	override fun findErrorRawVelocity(target: Vector2D) = target - rawVelocity
	override fun findErrorAcceleration(target: Vector2D) = target - acceleration
	override fun findErrorRawAcceleration(target: Vector2D) = target - rawAcceleration
}