package dev.frozenmilk.dairy.core.util.supplier.numeric

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.util.supplier.logical.Conditional
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.Supplier

interface IEnhancedNumberSupplier<N : Comparable<N>> : Feature {
	val supplier: Supplier<out N>
	val modify: (N) -> N
	val lowerDeadzone: N
	val upperDeadzone: N

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
	fun <N2> merge(supplier: Supplier<out N2>, merge: (N, N2) -> N): IEnhancedNumberSupplier<N>

	/**
	 * non-mutating
	 */
	fun applyModifier(modify: (N) -> N): IEnhancedNumberSupplier<N> //= EnhancedSupplier(this.supplier, modify, this.lowerDeadzone, this.upperDeadzone)

	/**
	 * non-mutating
	 */
	fun applyDeadzone(deadzone: N): IEnhancedNumberSupplier<N>//= EnhancedSupplier(this.supplier, this.modify, -(deadzone.coerceAtLeast(0.0)), deadzone.coerceAtLeast(0.0))

	/**
	 * non-mutating
	 */
	fun applyDeadzone(lowerDeadzone: N, upperDeadzone: N): IEnhancedNumberSupplier<N>//= EnhancedSupplier(this.supplier, this.modify, lowerDeadzone.coerceAtMost(0.0), upperDeadzone.coerceAtLeast(0.0))

	/**
	 * non-mutating
	 */
	fun applyLowerDeadzone(lowerDeadzone: N): IEnhancedNumberSupplier<N>//= EnhancedSupplier(this.supplier, this.modify, lowerDeadzone.coerceAtMost(0.0), this.upperDeadzone)

	/**
	 * non-mutating
	 */
	fun applyUpperDeadzone(upperDeadzone: N): IEnhancedNumberSupplier<N>//= EnhancedSupplier(this.supplier, this.modify, this.lowerDeadzone, upperDeadzone.coerceAtLeast(0.0))
	fun conditionalBindPosition(): Conditional<N>
	fun conditionalBindVelocity(): Conditional<N>
	fun conditionalBindVelocityRaw(): Conditional<N>
	fun conditionalBindAcceleration(): Conditional<N>
	fun conditionalBindAccelerationRaw(): Conditional<N>
	override fun preUserInitHook(opMode: Wrapper)

	override fun preUserInitLoopHook(opMode: Wrapper)

	override fun preUserStartHook(opMode: Wrapper)

	override fun preUserLoopHook(opMode: Wrapper)

	override fun preUserStopHook(opMode: Wrapper)

	override fun postUserStopHook(opMode: Wrapper)
}