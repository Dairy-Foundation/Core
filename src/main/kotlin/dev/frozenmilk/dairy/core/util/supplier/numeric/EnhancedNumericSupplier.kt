package dev.frozenmilk.dairy.core.util.supplier.numeric

import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.modifier.Modifier
import dev.frozenmilk.util.units.VelocityPacket
import java.util.function.Supplier

/**
 * [deregister]s at the end of the OpMode
 */
abstract class EnhancedNumericSupplier<N> @JvmOverloads constructor(override val supplier: Supplier<out N>, override val modifier: Modifier<N> = Modifier { x -> x }) : IEnhancedNumericSupplier<N> {
	/**
	 * a value that represents 0
	 */
	protected abstract val zero: N
	/**
	 * internal handler for current measurement
	 */
	protected abstract var current: N

	/**
	 * non-raw velocity is measured across a window of this width, in seconds
	 *
	 * defaults to 20 milliseconds
	 */
	override var measurementWindow = 0.02
	private var valid = false
	/**
	 * allows invalidation of the cache manually
	 */
	override fun invalidate() {
		valid = false
	}
	protected val previousPositions by lazy { ArrayDeque(listOf(VelocityPacket(current, current, System.nanoTime() / 1e9, System.nanoTime() / 1e9))) }
	protected val previousVelocities by lazy { ArrayDeque(listOf(VelocityPacket(zero, zero, System.nanoTime() / 1e9, System.nanoTime() / 1e9))) }
	protected fun update() {
		current = modifier.modify(supplier.get())
		val currentTime = System.nanoTime() / 1e9
		while (previousPositions.size >= 2 && currentTime - previousPositions[1].deltaTime >= measurementWindow) {
			previousPositions.removeFirst()
		}
		previousPositions.addLast(VelocityPacket(previousPositions.last().end, current, previousPositions.last().endTime, currentTime))
		while (previousVelocities.size >= 2 && currentTime - previousVelocities[1].deltaTime >= measurementWindow) {
			previousVelocities.removeFirst()
		}
		previousVelocities.addLast(VelocityPacket(previousVelocities.last().end, rawVelocity, previousVelocities.last().endTime, currentTime))
	}
	protected fun get(): N {
		if (!valid) {
			update()
			valid = true
		}
		return current
	}

	override fun component(motionComponent: MotionComponents) =
			when (motionComponent) {
				MotionComponents.POSITION -> position
				MotionComponents.VELOCITY -> velocity
				MotionComponents.RAW_VELOCITY -> rawVelocity
				MotionComponents.ACCELERATION -> acceleration
				MotionComponents.RAW_ACCELERATION -> rawAcceleration
			}
	override fun componentError(motionComponent: MotionComponents, target: N) =
			when (motionComponent) {
				MotionComponents.POSITION -> findErrorPosition(target)
				MotionComponents.VELOCITY -> findErrorVelocity(target)
				MotionComponents.RAW_VELOCITY -> findErrorRawVelocity(target)
				MotionComponents.ACCELERATION -> findErrorAcceleration(target)
				MotionComponents.RAW_ACCELERATION -> findErrorRawAcceleration(target)
			}

	//
	// Impl Feature:
	//
	override val dependencies by lazy {
		DependencySet(this)
				.yields()
	}

	init {
		@Suppress("LeakingThis")
		register()
	}

	/**
	 * if this automatically updates, by calling [invalidate] and [get]
	 */
	override var autoUpdates = true
	private fun autoUpdatePre() {
		if (autoUpdates) {
			get()
		}
	}
	private fun autoUpdatePost() {
		if (autoUpdates) {
			invalidate()
		}
	}

	override fun preUserInitHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserInitHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserInitLoopHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserInitLoopHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserStartHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserStartHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserLoopHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserLoopHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserStopHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserStopHook(opMode: Wrapper) {
		deregister()
	}
}