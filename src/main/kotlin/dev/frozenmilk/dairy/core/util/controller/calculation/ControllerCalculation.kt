package dev.frozenmilk.dairy.core.util.controller.calculation

import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier

interface ControllerCalculation<T: Any> : ControllerComponent<T, T> {
	/**
	 * Should be called every loop if this is in use, but it will not be evaluated.
	 * @see evaluate
	 *
	 * E.g. If this is in a branch, and this arm of the branch is not to be [evaluate]d, this will be [update]d instead.
	 *
	 * Used to perform necessary loop-by-loop updates to internal state, i.e. time deltas.
	 *
	 * @param accumulation the thus far accumulated outputs of [ControllerCalculation]s
	 * @param state the current state of the system
	 * @param target the target of the system
	 * @param deltaTime change in time, measured in seconds
	 */
	fun update(accumulation: T, state: MotionComponentSupplier<out T>, target: MotionComponentSupplier<out T>, error: MotionComponentSupplier<out T>, deltaTime: Double)

	/**
	 * Only gets called when the actual result of calculation this represents is needed.
	 * @see update
	 *
	 * E.g. If this is in a branch, and this arm of the branch is not to be [evaluate]d, this will be [update]d instead.
	 *
	 * @param accumulation the thus far accumulated outputs of [ControllerCalculation]s
	 * @param state the current state of the system
	 * @param target the target of the system
	 * @param error the error between target and state
	 * @param deltaTime change in time, measured in seconds
	 *
	 * @return [accumulation] + output
	 */
	override fun evaluate(accumulation: T, state: MotionComponentSupplier<out T>, target: MotionComponentSupplier<out T>, error: MotionComponentSupplier<out T>, deltaTime: Double): T

	/**
	 * called by the end user to notify this component that it should reset its internal state back to its starting defaults
	 */
	fun reset()

	/**
	 * called by tbe controller to notify this component that the target supplier has changed.
	 *
	 * @param newTarget the new target supplier of the controller
	 */
	fun targetChanged(newTarget: MotionComponentSupplier<out T>)

	operator fun plus(toAdd: ControllerCalculation<T>): ControllerCalculation<T> = object : ControllerCalculation<T> {
		override fun update(
			accumulation: T,
			state: MotionComponentSupplier<out T>,
			target: MotionComponentSupplier<out T>,
			error: MotionComponentSupplier<out T>,
			deltaTime: Double
		) {
			this@ControllerCalculation.update(accumulation, state, target, error, deltaTime)
			toAdd.update(accumulation, state, target, error, deltaTime)
		}

		override fun evaluate(
			accumulation: T,
			state: MotionComponentSupplier<out T>,
			target: MotionComponentSupplier<out T>,
			error: MotionComponentSupplier<out T>,
			deltaTime: Double
		): T = toAdd.evaluate(this@ControllerCalculation.evaluate(accumulation, state, target, error, deltaTime), state, target, error, deltaTime)

		override fun reset() {
			this@ControllerCalculation.reset()
			toAdd.reset()
		}

		override fun targetChanged(newTarget: MotionComponentSupplier<out T>) {
			this@ControllerCalculation.targetChanged(newTarget)
			toAdd.targetChanged(newTarget)
		}
	}
}
