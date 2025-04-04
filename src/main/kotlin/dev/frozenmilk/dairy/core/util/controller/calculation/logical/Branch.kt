@file:JvmName("Branch")
package dev.frozenmilk.dairy.core.util.controller.calculation.logical

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerComponent
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier

/**
 * @param map must not contain duplicate values (each [ControllerCalculation] must be unique)
 *
 * matches [this] -> [ControllerCalculation] via [map] or returns accumulation (no-op)
 */
fun <T: Any, R: Any> ControllerComponent<T, R>.map(map: Map<R, ControllerCalculation<T>>): ControllerCalculation<T> {
	if (map.values.size != map.values.distinct().size) throw IllegalArgumentException("map must not contain the same ControllerCalculation twice")
	return object : ControllerCalculation<T> {
		override fun update(
			accumulation: T,
			state: MotionComponentSupplier<out T>,
			target: MotionComponentSupplier<out T>,
			error: MotionComponentSupplier<out T>,
			deltaTime: Double
		) {
			map.forEach { (_, v) -> v.update(accumulation, state, target, error, deltaTime) }
		}

		override fun evaluate(
			accumulation: T,
			state: MotionComponentSupplier<out T>,
			target: MotionComponentSupplier<out T>,
			error: MotionComponentSupplier<out T>,
			deltaTime: Double
		): T {
			val selected = this@map.evaluate(accumulation, state, target, error, deltaTime)
			val selectedHash = selected.hashCode()
			var res: T = accumulation
			map.forEach { (k, v) ->
				if (k.hashCode() == selectedHash) res =
					v.evaluate(accumulation, state, target, error, deltaTime)
				else v.update(accumulation, state, target, error, deltaTime)
			}
			return res
		}

		override fun reset() {
			map.forEach { (_, v) -> v.reset() }
		}
		override fun targetChanged(newTarget: MotionComponentSupplier<out T>) {
			map.forEach { (_, v) -> v.targetChanged(newTarget) }
		}
	}
}

/**
 * @param map must not contain duplicate values (each [ControllerCalculation] must be unique), and must not contain[default]
 *
 * matches [this] -> [ControllerCalculation] via [map] or returns [default]
 */
fun <T: Any, R: Any> ControllerComponent<T, R>.mapOrDefault(map: Map<R, ControllerCalculation<T>>, default: ControllerCalculation<T>): ControllerCalculation<T> {
	if (map.values.size != map.values.distinct().size) throw IllegalArgumentException("map must not contain the same ControllerCalculation twice")
	if (map.containsValue(default)) throw IllegalArgumentException("map must not contain the default ControllerCalculation")
	return object : ControllerCalculation<T> {
		override fun update(
			accumulation: T,
			state: MotionComponentSupplier<out T>,
			target: MotionComponentSupplier<out T>,
			error: MotionComponentSupplier<out T>,
			deltaTime: Double
		) {
			map.forEach { (_, v) -> v.update(accumulation, state, target, error, deltaTime) }
		}

		override fun evaluate(
			accumulation: T,
			state: MotionComponentSupplier<out T>,
			target: MotionComponentSupplier<out T>,
			error: MotionComponentSupplier<out T>,
			deltaTime: Double
		): T {
			val selected = this@mapOrDefault.evaluate(accumulation, state, target, error, deltaTime)
			val selectedHash = selected.hashCode()
			var res: T? = null
			map.forEach { (k, v) ->
				if (k.hashCode() == selectedHash) res = v.evaluate(accumulation, state, target, error, deltaTime)
				else v.update(accumulation, state, target, error, deltaTime)
			}
			return if(res == null) {
				default.evaluate(accumulation, state, target, error, deltaTime)
			} else {
				default.update(accumulation, state, target, error, deltaTime)
				res!!
			}
		}

		override fun reset() {
			map.forEach { (_, v) -> v.reset() }
		}
		override fun targetChanged(newTarget: MotionComponentSupplier<out T>) {
			map.forEach { (_, v) -> v.targetChanged(newTarget) }
		}
	}
}
//
///**
// * simplified [Branch]
// */
//fun <T, R> Function<T, R>.branchAccumulation() = ControllerComponent { accumulation, _, _, _, _ -> apply(accumulation) }
///**
// * simplified [Branch]
// */
//class State<T, R>(cond: Function<MotionComponentSupplier<T>, R>) : Branch<T, R>(ControllerComponent { _, currentState, _, _, _ -> cond.apply(currentState) }) {
//	constructor(motionComponent: MotionComponents, cond: Function<T, R>) : this({ cond.apply(it.component(motionComponent)) })
//}
///**
// * simplified [Branch]
// */
//class Target<T, R>(cond: Function<MotionComponentSupplier<T>, R>) : Branch<T, R>(ControllerComponent { _, _, target, _, _ -> cond.apply(target) }) {
//	constructor(motionComponent: MotionComponents, cond: Function<T, R>) : this({ cond.apply(it.component(motionComponent)) })
//}
///**
// * simplified [Branch]
// */
//class Error<T, R>(cond: Function<MotionComponentSupplier<T>, R>) : Branch<T, R>(ControllerComponent { _, _, _, error, _ -> cond.apply(error) }) {
//	constructor(motionComponent: MotionComponents, cond: Function<T, R>) : this({ cond.apply(it.component(motionComponent)) })
//}
///**
// * simplified [Branch]
// */
//class DeltaTime<T, R>(cond: Function<Double, R>) : Branch<T, R>(ControllerComponent { _, _, _, _, deltaTime -> cond.apply(deltaTime) })