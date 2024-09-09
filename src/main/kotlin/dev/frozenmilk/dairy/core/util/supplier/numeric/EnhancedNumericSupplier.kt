package dev.frozenmilk.dairy.core.util.supplier.numeric

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.units.VelocityPacket
import java.util.function.Supplier

/**
 * [deregister]s at the end of the OpMode
 */
abstract class EnhancedNumericSupplier<N> : IEnhancedNumericSupplier<N>, Feature {
	abstract val supplier: Supplier<out N>
	/**
	 * a value that represents 0
	 */
	protected abstract val zero: N
	/**
	 * internal handler for current measurement
	 */
	protected abstract var currentState: N

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
	protected val previousPositions by lazy { ArrayDeque(listOf(VelocityPacket(currentState, currentState, System.nanoTime() / 1e9, System.nanoTime() / 1e9))) }
	protected val previousVelocities by lazy { ArrayDeque(listOf(VelocityPacket(zero, zero, System.nanoTime() / 1e9, System.nanoTime() / 1e9))) }
	private fun update() {
		currentState = supplier.get()
		val currentTime = System.nanoTime() / 1e9
		while (previousPositions.size >= 2 && currentTime - previousPositions[1].deltaTime >= measurementWindow) {
			previousPositions.removeFirst()
		}
		previousPositions.addLast(VelocityPacket(previousPositions.last().end, currentState, previousPositions.last().endTime, currentTime))
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
		return currentState
	}

	override fun get(motionComponent: MotionComponents) =
			when (motionComponent) {
				MotionComponents.STATE -> state
				MotionComponents.VELOCITY -> velocity
				MotionComponents.RAW_VELOCITY -> rawVelocity
				MotionComponents.ACCELERATION -> acceleration
				MotionComponents.RAW_ACCELERATION -> rawAcceleration
			}

	//
	// Impl Feature:
	//
	override var dependency: Dependency<*> = Yielding

	init {
		@Suppress("LeakingThis")
		register()
	}

	/**
	 * if [state] is automatically recalculated each loop
	 *
	 * ensures that the calculation is updated every loop by:
	 * - [get]ing the output (ensuring that it is evaluated if it hadn't been before)
	 * - [invalidate]ing the output (ensuring that it will be re-evaluated on-demand)
	 *
	 * should most likely be left true
	 */
	var autoCalculates = true
	private fun autoCalculatePost() {
		if (autoCalculates) {
			invalidate()
			get()
		}
	}

	override fun postUserInitHook(opMode: Wrapper) = autoCalculatePost()
	override fun postUserInitLoopHook(opMode: Wrapper) = autoCalculatePost()
	override fun postUserStartHook(opMode: Wrapper) = autoCalculatePost()
	override fun postUserLoopHook(opMode: Wrapper) = autoCalculatePost()
	override fun cleanup(opMode: Wrapper) {
		deregister()
	}
}