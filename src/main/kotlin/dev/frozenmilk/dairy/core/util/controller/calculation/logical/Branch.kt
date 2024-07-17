package dev.frozenmilk.dairy.core.util.controller.calculation.logical

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerComponent
import java.util.function.Function

open class Branch<T, R>(val cond: ControllerComponent<T, R>) {
	/**
	 * matches [cond] -> [ControllerCalculation] via [map] or returns accumulation (no-op)
	 */
	fun map(map: Map<R, ControllerCalculation<T>>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		return@ControllerCalculation map[cond.evaluate(accumulation, currentState, target, error, deltaTime)]?.evaluate(accumulation, currentState, target, error, deltaTime) ?: accumulation
	}

	/**
	 * matches [cond] -> [ControllerCalculation] via [map] or [default] if no [map] entry was found
	 */
	fun mapOrElse(map: Map<R, ControllerCalculation<T>>, default: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		return@ControllerCalculation map[cond.evaluate(accumulation, currentState, target, error, deltaTime)]?.evaluate(accumulation, currentState, target, error, deltaTime) ?: default.evaluate(accumulation, currentState, target, error, deltaTime)
	}

	/**
	 * matches [cond] -> [ControllerCalculation] via [map] or returns accumulation (no-op)
	 *
	 * evaluates all [ControllerCalculation]s in [map], regardless of which is used.
	 * this is useful for when a calculation is cheap, but may cause issues if it has a big gap in evaluation,
	 * e.g. D terms where previous error and delta time may be differently timestamped
	 *
	 * if some components need to be forced, and some don't, create two [ControllerCalculation] from this branch, using [map] and [forceMap]
	 */
	fun forceMap(map: Map<R, ControllerCalculation<T>>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		var res = accumulation
		val branch = cond.evaluate(accumulation, currentState, target, error, deltaTime)
		map.forEach { (key, calc) ->
			val eval = calc.evaluate(accumulation, currentState, target, error, deltaTime)
			if (key == branch) res = eval
		}
		res
	}

	/**
	 * matches [cond] -> [ControllerCalculation] via [map] or [default] if no [map] entry was found
	 *
	 * evaluates all [ControllerCalculation]s in [map], as well as [default], regardless of which is used.
	 * this is useful for when a calculation is cheap, but may cause issues if it has a big gap in evaluation,
	 * e.g. D terms where previous error and delta time may be differently timestamped
	 *
	 * if some components need to be forced, and some don't, create two [ControllerCalculation] from this branch, using [map] and [forceMap]
	 */
	fun forceMapOrElse(map: Map<R, ControllerCalculation<T>>, default: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		var res = default.evaluate(accumulation, currentState, target, error, deltaTime)
		val branch = cond.evaluate(accumulation, currentState, target, error, deltaTime)
		map.forEach { (key, calc) ->
			val eval = calc.evaluate(accumulation, currentState, target, error, deltaTime)
			if (key == branch) res = eval
		}
		res
	}

	/**
	 * simplified [Branch]
	 */
	class Accumulation<T, R>(cond: Function<T, R>) : Branch<T, R>(ControllerComponent { accumulation, _, _, _, _ -> cond.apply(accumulation) })
	/**
	 * simplified [Branch]
	 */
	class State<T, R>(cond: Function<T, R>) : Branch<T, R>(ControllerComponent { _, currentState, _, _, _ -> cond.apply(currentState) })
	/**
	 * simplified [Branch]
	 */
	class Target<T, R>(cond: Function<T, R>) : Branch<T, R>(ControllerComponent { _, _, target, _, _ -> cond.apply(target) })
	/**
	 * simplified [Branch]
	 */
	class Error<T, R>(cond: Function<T, R>) : Branch<T, R>(ControllerComponent { _, _, _, error, _ -> cond.apply(error) })
	/**
	 * simplified [Branch]
	 */
	class DeltaTime<T, R>(cond: Function<Double, R>) : Branch<T, R>(ControllerComponent { _, _, _, _, deltaTime -> cond.apply(deltaTime) })
}