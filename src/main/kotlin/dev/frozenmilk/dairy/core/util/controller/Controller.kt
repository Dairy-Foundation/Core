package dev.frozenmilk.dairy.core.util.controller

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.cell.LazyCell
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * @param targetSupplier supplier for the target position
 * @param inputSupplier supplier for the system state
 * @param motionComponent motionComponent that this controller will act on
 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
 * @param outputConsumer method to update the output consumer of the system
 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [output]
 *
 * [deregister]s at the end of the OpMode,
 * it is recommended to either re-[register] if you wish to re-use this,
 * or regenerate for each OpMode (which must be done if this is built from regenerated resourced, like motors or encoders)
 */
abstract class Controller<T> @JvmOverloads constructor(
	var targetSupplier: Supplier<out T>,
	var inputSupplier: IEnhancedNumericSupplier<T>,
	var motionComponent: MotionComponents,
	var toleranceEpsilon: T,
	var outputConsumer: Consumer<T> = Consumer {},
	var controllerCalculation: ControllerCalculation<T>,
) : Feature {
	/**
	 * @param target target position
	 * @param inputSupplier supplier for the system state
	 * @param motionComponent motionComponent that this controller will act on
	 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [output]
	 */
	@JvmOverloads
	constructor(
		target: T,
		inputSupplier: IEnhancedNumericSupplier<T>,
		motionComponent: MotionComponents,
		toleranceEpsilon: T,
		outputConsumer: Consumer<T> = Consumer {},
		controllerCalculation: ControllerCalculation<T>,
	) : this (Supplier { target }, inputSupplier, motionComponent, toleranceEpsilon, outputConsumer, controllerCalculation)

	override val dependency: Dependency<*> = Yielding
	init {
		@Suppress("LeakingThis")
		register()
	}

	private var previousTime = System.nanoTime()
	protected abstract val zero: T

	var target: T
		get() = targetSupplier.get()
		set(value) {
			targetSupplier = Supplier { value }
		}

	fun invalidate() {
		outputCell.invalidate()
	}

	private val outputCell = LazyCell {
		val currentTime = System.nanoTime()
		val deltaTime = (currentTime - previousTime) / 1e9
		val target = this.targetSupplier.get()
		val res = controllerCalculation.evaluate(zero, inputSupplier.position, targetSupplier.get(), inputSupplier.componentError(motionComponent, target), deltaTime)
		previousTime = currentTime
		res
	}

	/**
	 * the typed output of this controller, useful for piping it to another
	 */
	val output by outputCell

	fun update() {
		outputConsumer.accept(output)
	}

	/**
	 * error for [inputSupplier] and [motionComponent]
	 *
	 * if not [target] is passed, uses the current system target
	 */
	@JvmOverloads
	fun error(target: T = this.target) = inputSupplier.componentError(motionComponent, target)

	/**
	 * @return if this controller has finished within variance of [toleranceEpsilon]
	 */
	abstract fun finished(toleranceEpsilon: T): Boolean

	/**
	 * [finished] but uses internal [toleranceEpsilon]
	 */
	fun finished() = finished(toleranceEpsilon)

	/**
	 * if this automatically updates the calculation, by calling [invalidate]
	 *
	 * this should be left `true`, as it ensures that [output] is lazily re-calculated once / loop, and is cheap to run.
	 *
	 * if this is set to `false`, then the controller can only be updated manually.
	 *
	 * [enabled] should be set to false, and this should be left true for piping this to another [Controller].
	 *
	 * @see enabled
	 */
	var autoCalc = true
	/**
	 * if this automatically updates in [dev.frozenmilk.dairy.core.wrapper.Wrapper.OpModeState.ACTIVE] (loop), by calling [update]
	 *
	 * if this is false, the controller will not update [outputConsumer] automatically.
	 *
	 * @see autoCalc
	 */
	var enabled = true
	private fun autoCalcInvalidate() {
		if (autoCalc) {
			invalidate()
		}
	}
	private fun autoUpdate() {
		autoCalcInvalidate()
		if (enabled) {
			update()
		}
	}

	override fun postUserInitHook(opMode: Wrapper) = autoCalcInvalidate()
	override fun postUserInitLoopHook(opMode: Wrapper) = autoCalcInvalidate()
	override fun postUserStartHook(opMode: Wrapper) = autoCalcInvalidate()
	override fun postUserLoopHook(opMode: Wrapper) = autoUpdate()
	override fun postUserStopHook(opMode: Wrapper) {
		deregister()
	}

	/**
	 * an [EnhancedNumericSupplier] built off the output of this controller, useful for piping the output of this controller to another
	 */
	abstract val supplier: EnhancedNumericSupplier<T>
}