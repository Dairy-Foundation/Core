package dev.frozenmilk.dairy.core.util.supplier.numeric

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.util.modifier.Modifier
import java.util.function.Supplier

interface IEnhancedNumericSupplier<N> : Feature {
	val supplier: Supplier<out N>
	val modifier: Modifier<N>
	/**
	 * position
	 */
	var position: N

	/**
	 * velocity with a filter applied by looking at velocity over the last [measurementWindow] seconds
	 */
	val velocity: N

	/**
	 * [velocity] with no filter applied
	 */
	val rawVelocity: N

	/**
	 * non-raw velocity and acceleration is measured across a window of this width, in seconds
	 *
	 * defaults to 20 milliseconds
	 */
	var measurementWindow: Double

	/**
	 * acceleration with a filter applied by looking at acceleration over the last [measurementWindow] seconds
	 */
	val acceleration: N

	/**
	 * [acceleration] with no filter applied
	 */
	val rawAcceleration: N

	//
	// Impl Feature:
	//
	/**
	 * if this automatically updates, by calling [invalidate] and [get]
	 */
	var autoUpdates: Boolean

	/**
	 * allows invalidation of the cache manually
	 */
	fun invalidate()
	fun findErrorPosition(target: N): N
	fun findErrorVelocity(target: N): N
	fun findErrorRawVelocity(target: N): N
	fun findErrorAcceleration(target: N): N
	fun findErrorRawAcceleration(target: N): N
	fun component(motionComponent: MotionComponents): N
	fun componentError(motionComponent: MotionComponents, target: N): N
	fun <N2> merge(supplier: Supplier<out N2>, merge: (N, N2) -> N): IEnhancedNumericSupplier<N>

	/**
	 * applies another modifier on top of this supplier
	 *
	 * non-mutating
	 *
	 * @see setModifier
	 */
	fun applyModifier(modifier: Modifier<N>): IEnhancedNumericSupplier<N>

	/**
	 * non-mutating
	 *
	 * overrides the previous modifier
	 *
	 * @see applyModifier
	 */
	fun setModifier(modifier: Modifier<N>): IEnhancedNumericSupplier<N>
}