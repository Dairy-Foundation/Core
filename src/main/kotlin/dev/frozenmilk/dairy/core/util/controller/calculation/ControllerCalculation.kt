package dev.frozenmilk.dairy.core.util.controller.calculation

@FunctionalInterface
fun interface ControllerCalculation<T> : ControllerComponent<T, T> {
	/**
	 * @param accumulation the thus far accumulated outputs of [ControllerCalculation]s, it is important to return this value in the result
	 * @param currentState the current state of the system, dependant on [dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents]
	 * @param target the target of the system
	 * @param error the error of the system, this ensures that special cases for error are handled (e.g. in [dev.frozenmilk.util.units.angle.Angle]s)
	 * @param deltaTime change in time, measured in seconds
	 *
	 * @return [accumulation] + output
	 */
	override fun evaluate(accumulation: T, currentState: T, target: T, error: T, deltaTime: Double): T

	operator fun plus(toAdd: ControllerCalculation<T>) = ControllerCalculation<T> { accumulation, currentState, target, error, deltaTime ->
		toAdd.evaluate(evaluate(accumulation, currentState, target, error, deltaTime), currentState, target, error, deltaTime)
	}
}
