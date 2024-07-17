package dev.frozenmilk.dairy.core.util.controller.calculation.logical

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerComponent
import java.util.function.Function

open class BinaryBranch<T>(val cond: ControllerComponent<T, Boolean>) {
	/**
	 * if [cond]:
	 *
	 * true -> [evalTrue]
	 *
	 * false -> [evalTrue]
	 */
	fun eval(evalTrue: ControllerCalculation<T>, evalFalse: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		return@ControllerCalculation if(cond.evaluate(accumulation, currentState, target, error, deltaTime)) {
			evalTrue.evaluate(accumulation, currentState, target, error, deltaTime)
		}
		else {
			evalFalse.evaluate(accumulation, currentState, target, error, deltaTime)
		}
	}
	/**
	 * if [cond]:
	 *
	 * true -> [evalTrue]
	 *
	 * false -> [evalTrue]
	 *
	 * evaluates both [evalTrue] and [evalFalse], regardless of which is used.
	 * this is useful for when a calculation is cheap, but may cause issues if it has a big gap in evaluation,
	 * e.g. D terms where previous error and delta time may be differently timestamped
	 */
	fun forceEval(evalTrue: ControllerCalculation<T>, evalFalse: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		return@ControllerCalculation if(cond.evaluate(accumulation, currentState, target, error, deltaTime)) {
			evalFalse.evaluate(accumulation, currentState, target, error, deltaTime)
			evalTrue.evaluate(accumulation, currentState, target, error, deltaTime)
		}
		else {
			evalTrue.evaluate(accumulation, currentState, target, error, deltaTime)
			evalFalse.evaluate(accumulation, currentState, target, error, deltaTime)
		}
	}
	/**
	 * if [cond]:
	 *
	 * true -> [evalTrue]
	 *
	 * false -> accumulation (no-op)
	 */
	fun evalTrue(evalTrue: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		if (cond.evaluate(accumulation, currentState, target, error, deltaTime)) {
			evalTrue.evaluate(accumulation, currentState, target, error, deltaTime)
		}
		else {
			accumulation
		}
	}
	/**
	 * if [cond]:
	 *
	 * true -> [evalTrue]
	 *
	 * false -> accumulation (no-op)
	 *
	 * evaluates [evalTrue], regardless of if it is used.
	 * this is useful for when a calculation is cheap, but may cause issues if it has a big gap in evaluation,
	 * e.g. D terms where previous error and delta time may be differently timestamped
	 */
	fun forceEvalTrue(evalTrue: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		return@ControllerCalculation if (cond.evaluate(accumulation, currentState, target, error, deltaTime)) {
			evalTrue.evaluate(accumulation, currentState, target, error, deltaTime)
		}
		else {
			evalTrue.evaluate(accumulation, currentState, target, error, deltaTime)
			accumulation
		}
	}	/**
	 * if [cond]:
	 *
	 * true -> accumulation (no-op)
	 *
	 * false -> [evalFalse]
	 */
	fun evalFalse(evalFalse: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		return@ControllerCalculation if (cond.evaluate(accumulation, currentState, target, error, deltaTime)) {
			accumulation
		}
		else {
			evalFalse.evaluate(accumulation, currentState, target, error, deltaTime)
		}
	}
	/**
	 * if [cond]:
	 *
	 * true -> accumulation (no-op)
	 *
	 * false -> [evalFalse]
	 *
	 * evaluates [evalFalse], regardless of if it is used.
	 * this is useful for when a calculation is cheap, but may cause issues if it has a big gap in evaluation,
	 * e.g. D terms where previous error and delta time may be differently timestamped
	 */
	fun forceEvalFalse(evalFalse: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		return@ControllerCalculation if (cond.evaluate(accumulation, currentState, target, error, deltaTime)) {
			evalFalse.evaluate(accumulation, currentState, target, error, deltaTime)
			accumulation
		}
		else {
			evalFalse.evaluate(accumulation, currentState, target, error, deltaTime)
		}
	}

	/**
	 * simplified [BinaryBranch]
	 */
	class Accumulation<T>(cond: Function<T, Boolean>) : BinaryBranch<T>(ControllerComponent { accumulation, _, _, _, _ -> cond.apply(accumulation) })
	/**
	 * simplified [BinaryBranch]
	 */
	class State<T>(cond: Function<T, Boolean>) : BinaryBranch<T>(ControllerComponent { _, currentState, _, _, _ -> cond.apply(currentState) })
	/**
	 * simplified [BinaryBranch]
	 */
	class Target<T>(cond: Function<T, Boolean>) : BinaryBranch<T>(ControllerComponent { _, _, target, _, _ -> cond.apply(target) })
	/**
	 * simplified [BinaryBranch]
	 */
	class Error<T>(cond: Function<T, Boolean>) : BinaryBranch<T>(ControllerComponent { _, _, _, error, _ -> cond.apply(error) })
	/**
	 * simplified [BinaryBranch]
	 */
	class DeltaTime<T>(cond: Function<Double, Boolean>) : BinaryBranch<T>(ControllerComponent { _, _, _, _, deltaTime -> cond.apply(deltaTime) })
}