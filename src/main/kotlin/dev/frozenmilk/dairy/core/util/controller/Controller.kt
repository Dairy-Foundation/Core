package dev.frozenmilk.dairy.core.util.controller

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.controller.implementation.MotionComponentConsumer
import dev.frozenmilk.dairy.core.util.supplier.numeric.CachedMotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.Consumer
import java.util.function.Supplier

@Suppress("INAPPLICABLE_JVM_NAME")
abstract class Controller<T : Any>
@JvmOverloads
constructor(
	val targetSupplier: MotionComponentSupplier<out T>,
	val stateSupplier: MotionComponentSupplier<out T>,
	val errorSupplier: CachedMotionComponentSupplier<out T>,
	var toleranceEpsilon: MotionComponentSupplier<out T>,
	var outputConsumer: MotionComponentConsumer<T> = MotionComponentConsumer {},
	val controllerCalculation: ControllerCalculation<T>,
) : EnhancedNumericSupplier<T>(), Feature {
	constructor(
		targetSupplier: MotionComponentSupplier<out T>,
		stateSupplier: MotionComponentSupplier<out T>,
		errorSupplier: CachedMotionComponentSupplier<out T>,
		toleranceEpsilon: MotionComponentSupplier<out T>,
		outputConsumer: Consumer<in T>,
		controllerCalculation: ControllerCalculation<T>,
	) : this(
		targetSupplier,
		stateSupplier,
		errorSupplier,
		toleranceEpsilon,
		{ outputConsumer.accept(it.get(MotionComponents.STATE)) },
		controllerCalculation,
	)

	private var previousTime = System.nanoTime()
	final override val supplier: Supplier<out T> = Supplier {
		val currentTime = System.nanoTime()
		val deltaTime = (currentTime - previousTime) / 1e9
		errorSupplier.reset()
		val res = controllerCalculation.evaluate(zero, stateSupplier, targetSupplier, errorSupplier, deltaTime)
		previousTime = currentTime
		res
	}
	override var currentState: T = supplier.get()

	/**
	 * the current output of the controller
	 */
	@get:JvmName("state")
	@set:JvmName("state")
	abstract override var state: T

	/**
	 * @return if this controller has finished within variance of [toleranceEpsilon]
	 */
	abstract fun finished(toleranceEpsilon: MotionComponentSupplier<out T>): Boolean

	/**
	 * [finished] but uses internal [toleranceEpsilon]
	 */
	fun finished() = finished(toleranceEpsilon)

	fun update() = outputConsumer.accept(this)

	/**
	 * if this automatically updates in [dev.frozenmilk.dairy.core.wrapper.Wrapper.OpModeState.ACTIVE] (loop), by calling [update]
	 *
	 * if this is false, the controller will not update [outputConsumer] automatically.
	 *
	 * @see autoCalculates
	 */
	var enabled = true

	override var dependency: Dependency<*> = Yielding

	override fun postUserInitLoopHook(opMode: Wrapper) {
		if (enabled) update()
		super<EnhancedNumericSupplier>.postUserInitLoopHook(opMode)
	}
	override fun postUserLoopHook(opMode: Wrapper) {
		if (enabled) update()
		super<EnhancedNumericSupplier>.postUserLoopHook(opMode)
	}
}
