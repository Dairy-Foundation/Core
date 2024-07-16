package dev.frozenmilk.dairy.core.util.controller.calculation.logical

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerComponent
import java.util.function.Function

open class Branch<T, R>(val cond: ControllerComponent<T, R>) {
	fun map(map: Map<R, ControllerCalculation<T>>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		return@ControllerCalculation map[cond.evaluate(accumulation, currentState, target, error, deltaTime)]?.evaluate(accumulation, currentState, target, error, deltaTime) ?: accumulation
	}

	class Accumulation<T, R>(cond: Function<T, R>) : Branch<T, R>(ControllerComponent { accumulation, _, _, _, _ -> cond.apply(accumulation) })
	class State<T, R>(cond: Function<T, R>) : Branch<T, R>(ControllerComponent { _, currentState, _, _, _ -> cond.apply(currentState) })
	class Target<T, R>(cond: Function<T, R>) : Branch<T, R>(ControllerComponent { _, _, target, _, _ -> cond.apply(target) })
	class Error<T, R>(cond: Function<T, R>) : Branch<T, R>(ControllerComponent { _, _, _, error, _ -> cond.apply(error) })
	class DeltaTime<T, R>(cond: Function<Double, R>) : Branch<T, R>(ControllerComponent { _, _, _, _, deltaTime -> cond.apply(deltaTime) })
}