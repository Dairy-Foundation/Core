@file:JvmName("ControllerComponents")
@file:JvmMultifileClass()
package dev.frozenmilk.dairy.core.util.controller.calculation

import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import java.util.function.DoubleFunction
import java.util.function.Function

@FunctionalInterface
fun interface ControllerComponent<T: Any, R> {
	/**
	 * @param accumulation the thus far accumulated outputs of [ControllerCalculation]s, it is important to return this value in the result
	 * @param state the current state of the system
	 * @param target the target of the system
	 * @param error the error between target and state
	 * @param deltaTime change in time, measured in seconds
	 *
	 * @return [accumulation] + output
	 */
	fun evaluate(accumulation: T, state: MotionComponentSupplier<out T>, target: MotionComponentSupplier<out T>, error: MotionComponentSupplier<out T>, deltaTime: Double): R
}
/**
 * simplified [ControllerComponent] to only use accumulation
 */
fun <T : Any, R> Function<T, R>.accumulationComponent() = ControllerComponent { accumulation, _, _, _, _ -> apply(accumulation) }
/**
 * simplified [ControllerComponent] to only use state
 */
fun <T : Any, R> Function<MotionComponentSupplier<out T>, R>.stateComponent() = ControllerComponent<T, R> { _, state, _, _, _ -> apply(state) }
/**
 * simplified [ControllerComponent] to only use target
 */
fun <T : Any, R> Function<MotionComponentSupplier<out T>, R>.targetComponent() = ControllerComponent<T, R> { _, _, target, _, _ -> apply(target) }
/**
 * simplified [ControllerComponent] to only use error
 */
fun <T : Any, R> Function<MotionComponentSupplier<out T>, R>.errorComponent() = ControllerComponent<T, R> { _, _, _, error, _ -> apply(error) }
/**
 * simplified [ControllerComponent] to only use deltaTime
 */
fun <T : Any, R> Function<Double, R>.deltaTimeComponent() = ControllerComponent<T, R> { _, _, _, _, deltaTime -> apply(deltaTime) }
/**
 * simplified [ControllerComponent] to only use deltaTime
 */
fun <T : Any, R> DoubleFunction<R>.deltaTimeComponent() = ControllerComponent<T, R> { _, _, _, _, deltaTime -> apply(deltaTime) }
