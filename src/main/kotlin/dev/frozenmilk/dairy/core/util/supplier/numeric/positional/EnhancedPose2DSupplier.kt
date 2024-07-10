package dev.frozenmilk.dairy.core.util.supplier.numeric.positional

import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedNumericSupplier
import dev.frozenmilk.util.modifier.Modifier
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import dev.frozenmilk.util.units.position.Pose2D
import java.util.function.Supplier

class EnhancedPose2DSupplier(supplier: Supplier<out Pose2D>, modifier: Modifier<Pose2D> = Modifier { x -> x }) : EnhancedNumericSupplier<Pose2D>(supplier, modifier) {
	override val zero = Pose2D()
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
	override fun setModifier(modifier: Modifier<Pose2D>) = EnhancedPose2DSupplier(this::position, modifier)
	override fun applyModifier(modifier: Modifier<Pose2D>) = EnhancedPose2DSupplier(supplier, modifier)
	override fun <N2> merge(supplier: Supplier<out N2>, merge: (Pose2D, N2) -> Pose2D) = EnhancedPose2DSupplier({ merge(get(), supplier.get()) }, modifier)
	override fun findErrorPosition(target: Pose2D) = target - position
	override fun findErrorVelocity(target: Pose2D) = target - velocity
	override fun findErrorRawVelocity(target: Pose2D) = target - rawVelocity
	override fun findErrorAcceleration(target: Pose2D) = target - acceleration
	override fun findErrorRawAcceleration(target: Pose2D) = target - rawAcceleration
}