package dev.frozenmilk.dairy.core.util.supplier.numeric.positional

import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedNumericSupplier
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import dev.frozenmilk.util.units.position.DistanceVector2D
import java.util.function.Supplier

@Suppress("INAPPLICABLE_JVM_NAME")
class EnhancedVector2DSupplier(override val supplier: Supplier<out DistanceVector2D>) : EnhancedNumericSupplier<DistanceVector2D>() {
	override val zero = DistanceVector2D()
	private var offset = zero
	override var currentState = supplier.get()
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
}