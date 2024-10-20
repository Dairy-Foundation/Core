@file:JvmName("BinaryBranch")
package dev.frozenmilk.dairy.core.util.controller.calculation.logical

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerComponent
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier


/**
 * if [this].[ControllerComponent.evaluate]:
 *
 * true -> [evalTrue]
 *
 * false -> [evalTrue]
 */
fun <T : Any> ControllerComponent<T, Boolean>.eval(evalTrue: ControllerCalculation<T>, evalFalse: ControllerCalculation<T>): ControllerCalculation<T> {
	if (evalTrue == evalFalse) throw IllegalArgumentException("evalTrue: $evalTrue must not be the same as evalFalse: $evalFalse")
	return object : ControllerCalculation<T> {
		override fun update(
			accumulation: T,
			state: MotionComponentSupplier<out T>,
			target: MotionComponentSupplier<out T>,
			error: MotionComponentSupplier<out T>,
			deltaTime: Double
		) {
			evalTrue.update(accumulation, state, target, error, deltaTime)
			evalFalse.update(accumulation, state, target, error, deltaTime)
		}

		override fun evaluate(
			accumulation: T,
			state: MotionComponentSupplier<out T>,
			target: MotionComponentSupplier<out T>,
			error: MotionComponentSupplier<out T>,
			deltaTime: Double
		): T = if (this@eval.evaluate(accumulation, state, target, error, deltaTime)) {
			evalFalse.update(accumulation, state, target, error, deltaTime)
			evalTrue.evaluate(accumulation, state, target, error, deltaTime)
		} else {
			evalTrue.update(accumulation, state, target, error, deltaTime)
			evalFalse.evaluate(accumulation, state, target, error, deltaTime)
		}

		override fun reset() {
			evalTrue.reset()
			evalFalse.reset()
		}
		override fun targetChanged(newTarget: MotionComponentSupplier<out T>) {
			evalTrue.targetChanged(newTarget)
			evalFalse.targetChanged(newTarget)
		}
	}
}
/**
 * if [this].[ControllerComponent.evaluate]:
 *
 * true -> [evalTrue]
 *
 * false -> accumulation (no-op)
 */
fun <T : Any> ControllerComponent<T, Boolean>.evalTrue(evalTrue: ControllerCalculation<T>): ControllerCalculation<T> {
	return object : ControllerCalculation<T> {
		override fun update(
			accumulation: T,
			state: MotionComponentSupplier<out T>,
			target: MotionComponentSupplier<out T>,
			error: MotionComponentSupplier<out T>,
			deltaTime: Double
		) = evalTrue.update(accumulation, state, target, error, deltaTime)

		override fun evaluate(
			accumulation: T,
			state: MotionComponentSupplier<out T>,
			target: MotionComponentSupplier<out T>,
			error: MotionComponentSupplier<out T>,
			deltaTime: Double
		): T = if (this@evalTrue.evaluate(accumulation, state, target, error, deltaTime)) {
			evalTrue.evaluate(accumulation, state, target, error, deltaTime)
		} else {
			evalTrue.update(accumulation, state, target, error, deltaTime)
			accumulation
		}

		override fun reset() = evalTrue.reset()
		override fun targetChanged(newTarget: MotionComponentSupplier<out T>) = evalTrue.targetChanged(newTarget)
	}
}

/**
 * if [this].[ControllerComponent.evaluate]:
 *
 * true -> accumulation (no-op)
 *
 * false -> [evalFalse]
 */
fun <T : Any> ControllerComponent<T, Boolean>.evalFalse(evalFalse: ControllerCalculation<T>): ControllerCalculation<T> {
	return object : ControllerCalculation<T> {
		override fun update(
			accumulation: T,
			state: MotionComponentSupplier<out T>,
			target: MotionComponentSupplier<out T>,
			error: MotionComponentSupplier<out T>,
			deltaTime: Double
		) = evalFalse.update(accumulation, state, target, error, deltaTime)

		override fun evaluate(
			accumulation: T,
			state: MotionComponentSupplier<out T>,
			target: MotionComponentSupplier<out T>,
			error: MotionComponentSupplier<out T>,
			deltaTime: Double
		): T = if (this@evalFalse.evaluate(accumulation, state, target, error, deltaTime)) {
			evalFalse.update(accumulation, state, target, error, deltaTime)
			accumulation
		}
		else {
			evalFalse.evaluate(accumulation, state, target, error, deltaTime)
		}

		override fun reset() = evalFalse.reset()
		override fun targetChanged(newTarget: MotionComponentSupplier<out T>) = evalFalse.targetChanged(newTarget)
	}
}
//
//open class BinaryBranch<T: Any>(val cond: ControllerComponent<T, Boolean>) {
//	/**
//	 * if [cond]:
//	 *
//	 * true -> [evalTrue]
//	 *
//	 * false -> [evalTrue]
//	 */
//	fun eval(evalTrue: ControllerCalculation<T>, evalFalse: ControllerCalculation<T>) = object : ControllerCalculation<T> {
//		override fun update(
//			accumulation: T,
//			state: MotionComponentSupplier<T>,
//			target: MotionComponentSupplier<T>,
//			error: MotionComponentSupplier<T>,
//			deltaTime: Double
//		) {
//			evalTrue.update(accumulation, state, target, error, deltaTime)
//			evalFalse.update(accumulation, state, target, error, deltaTime)
//		}
//
//		override fun evaluate(
//			accumulation: T,
//			state: MotionComponentSupplier<T>,
//			target: MotionComponentSupplier<T>,
//			error: MotionComponentSupplier<T>,
//			deltaTime: Double
//		) = if(cond.evaluate(accumulation, state, target, error, deltaTime)) {
//				evalFalse.update(accumulation, state, target, error, deltaTime)
//				evalTrue.evaluate(accumulation, state, target, error, deltaTime)
//			}
//			else {
//				evalTrue.update(accumulation, state, target, error, deltaTime)
//				evalFalse.evaluate(accumulation, state, target, error, deltaTime)
//			}
//
//		override fun reset() {
//			evalTrue.reset()
//			evalFalse.reset()
//		}
//	}
//	/**
//	 * simplified [BinaryBranch]
//	 */
//	class Accumulation<T: Any>(cond: Function<T, Boolean>) : BinaryBranch<T>(ControllerComponent { accumulation, _, _, _, _ -> cond.apply(accumulation) })
//	/**
//	 * simplified [BinaryBranch]
//	 */
//	class State<T: Any>(cond: Function<MotionComponentSupplier<T>, Boolean>) : BinaryBranch<T>(ControllerComponent { _, currentState, _, _, _ -> cond.apply(currentState) }) {
//		constructor(motionComponent: MotionComponents, cond: Function<T, Boolean>) : this({ cond.apply(it.component(motionComponent)) })
//	}
//	/**
//	 * simplified [BinaryBranch]
//	 */
//	class Target<T: Any>(cond: Function<MotionComponentSupplier<T>, Boolean>) : BinaryBranch<T>(ControllerComponent { _, _, target, _, _ -> cond.apply(target) }) {
//		constructor(motionComponent: MotionComponents, cond: Function<T, Boolean>) : this({ cond.apply(it.component(motionComponent)) })
//	}
//	/**
//	 * simplified [BinaryBranch]
//	 */
//	class Error<T: Any>(cond: Function<MotionComponentSupplier<T>, Boolean>) : BinaryBranch<T>(ControllerComponent { _, _, _, error, _ -> cond.apply(error) }) {
//		constructor(motionComponent: MotionComponents, cond: Function<T, Boolean>) : this({ cond.apply(it.component(motionComponent)) })
//	}
//	/**
//	 * simplified [BinaryBranch]
//	 */
//	class DeltaTime<T: Any>(cond: Function<Double, Boolean>) : BinaryBranch<T>(ControllerComponent { _, _, _, _, deltaTime -> cond.apply(deltaTime) })
//}