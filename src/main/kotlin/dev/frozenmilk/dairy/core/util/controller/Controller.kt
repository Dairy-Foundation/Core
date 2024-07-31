package dev.frozenmilk.dairy.core.util.controller

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.modifier.Modifier
import java.util.function.Consumer
import java.util.function.Supplier

abstract class Controller<T> @JvmOverloads constructor(
	var targetSupplier: MotionComponentSupplier<out T>,
	var inputSupplier: MotionComponentSupplier<T>,
	var motionComponent: MotionComponents,
	var toleranceEpsilon: T,
	var outputConsumer: Consumer<T> = Consumer {},
	var controllerCalculation: ControllerCalculation<T>,
	override val modifier: Modifier<T>
) : EnhancedNumericSupplier<T>(), Feature {
	private var previousTime = System.nanoTime()
	final override val supplier: Supplier<out T> = Supplier {
		val currentTime = System.nanoTime()
		val deltaTime = (currentTime - previousTime) / 1e9
		val target = targetSupplier.component(motionComponent)
		val res = modifier.modify(controllerCalculation.evaluate(zero, inputSupplier.component(motionComponent), target, inputSupplier.componentError(motionComponent, target), deltaTime))
		previousTime = currentTime
		res
	}
	override var current: T = supplier.get()

	/**
	 * the current output of the controller
	 */
	abstract override var position: T

	/**
	 * @return if this controller has finished within variance of [toleranceEpsilon]
	 */
	abstract fun finished(toleranceEpsilon: T): Boolean

	/**
	 * [finished] but uses internal [toleranceEpsilon]
	 */
	fun finished() = finished(toleranceEpsilon)

	abstract var target: T

	fun update() = outputConsumer.accept(component(motionComponent))
	/**
	 * if this automatically updates in [dev.frozenmilk.dairy.core.wrapper.Wrapper.OpModeState.ACTIVE] (loop), by calling [update]
	 *
	 * if this is false, the controller will not update [outputConsumer] automatically.
	 *
	 * @see autoUpdates
	 */
	var enabled = true

	/**
	 * if [position] is automatically recalculated each loop, and [invalidate]ing it at the start of each loop,
	 *
	 * should most likely be left true
	 *
	 * @see enabled
	 */
	override var autoUpdates = true
	private fun autoUpdatePre() {
		if (autoUpdates) {
			invalidate()
		}
	}
	private fun autoUpdatePost() {
		if (autoUpdates) {
			get()
		}
	}

	override var dependency: Dependency<*> = Yielding

	override fun preUserInitHook(opMode: Wrapper) {}
	override fun postUserInitHook(opMode: Wrapper) {}
	override fun preUserStartHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserStartHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserInitLoopHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserInitLoopHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserLoopHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserLoopHook(opMode: Wrapper) {
		autoUpdatePost()
		if (enabled) update()
	}
	override fun preUserStopHook(opMode: Wrapper) {}
	override fun postUserStopHook(opMode: Wrapper) {
		deregister()
	}
}
