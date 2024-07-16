package dev.frozenmilk.dairy.core.util.controller.calculation.logical

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerComponent
import java.util.function.Function

open class BinaryBranch<T>(val cond: ControllerComponent<T, Boolean>) {
	fun eval(evalTrue: ControllerCalculation<T>, evalFalse: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		return@ControllerCalculation if(cond.evaluate(accumulation, currentState, target, error, deltaTime)) {
			evalTrue.evaluate(accumulation, currentState, target, error, deltaTime)
		}
		else {
			evalFalse.evaluate(accumulation, currentState, target, error, deltaTime)
		}
	}
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
	fun evalTrue(evalTrue: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		if (cond.evaluate(accumulation, currentState, target, error, deltaTime)) {
			evalTrue.evaluate(accumulation, currentState, target, error, deltaTime)
		}
		else {
			accumulation
		}
	}
	fun forceEvalTrue(evalTrue: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		return@ControllerCalculation if (cond.evaluate(accumulation, currentState, target, error, deltaTime)) {
			evalTrue.evaluate(accumulation, currentState, target, error, deltaTime)
		}
		else {
			evalTrue.evaluate(accumulation, currentState, target, error, deltaTime)
			accumulation
		}
	}
	fun evalFalse(evalFalse: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		return@ControllerCalculation if (cond.evaluate(accumulation, currentState, target, error, deltaTime)) {
			accumulation
		}
		else {
			evalFalse.evaluate(accumulation, currentState, target, error, deltaTime)
		}
	}
	fun forceEvalFalse(evalFalse: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		return@ControllerCalculation if (cond.evaluate(accumulation, currentState, target, error, deltaTime)) {
			evalFalse.evaluate(accumulation, currentState, target, error, deltaTime)
			accumulation
		}
		else {
			evalFalse.evaluate(accumulation, currentState, target, error, deltaTime)
		}
	}

	class Accumulation<T>(cond: Function<T, Boolean>) : BinaryBranch<T>(ControllerComponent { accumulation, _, _, _, _ -> cond.apply(accumulation) })
	class State<T>(cond: Function<T, Boolean>) : BinaryBranch<T>(ControllerComponent { _, currentState, _, _, _ -> cond.apply(currentState) })
	class Target<T>(cond: Function<T, Boolean>) : BinaryBranch<T>(ControllerComponent { _, _, target, _, _ -> cond.apply(target) })
	class Error<T>(cond: Function<T, Boolean>) : BinaryBranch<T>(ControllerComponent { _, _, _, error, _ -> cond.apply(error) })
	class DeltaTime<T>(cond: Function<Double, Boolean>) : BinaryBranch<T>(ControllerComponent { _, _, _, _, deltaTime -> cond.apply(deltaTime) })
}