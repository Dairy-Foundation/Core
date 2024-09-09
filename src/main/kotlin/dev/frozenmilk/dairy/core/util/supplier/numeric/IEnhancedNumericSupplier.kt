package dev.frozenmilk.dairy.core.util.supplier.numeric

@Suppress("INAPPLICABLE_JVM_NAME")
interface IEnhancedNumericSupplier<N> : MotionComponentSupplier<N> {
	/**
	 * state
	 */
	@get:JvmName("state")
	@set:JvmName("state")
	var state: N

	/**
	 * velocity with a filter applied by looking at velocity over the last [measurementWindow] seconds
	 */
	@get:JvmName("velocity")
	val velocity: N

	/**
	 * [velocity] with no filter applied
	 */
	@get:JvmName("rawVelocity")
	val rawVelocity: N

	/**
	 * acceleration with a filter applied by looking at acceleration over the last [measurementWindow] seconds
	 */
	@get:JvmName("acceleration")
	val acceleration: N

	/**
	 * [acceleration] with no filter applied
	 */
	@get:JvmName("rawAcceleration")
	val rawAcceleration: N

	/**
	 * non-raw velocity and acceleration is measured across a window of this width, in seconds
	 *
	 * defaults to 20 milliseconds
	 */
	var measurementWindow: Double

	/**
	 * allows invalidation of the cache manually
	 */
	fun invalidate()
}