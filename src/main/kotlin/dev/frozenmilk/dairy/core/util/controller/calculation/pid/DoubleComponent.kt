package dev.frozenmilk.dairy.core.util.controller.calculation.pid

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation

class DoubleComponent private constructor() {
	class P (var kP: Double) : ControllerCalculation<Double> {
		override fun evaluate(
			accumulation: Double,
			currentState: Double,
			target: Double,
			error: Double,
			deltaTime: Double
		) = accumulation + (error * kP)
	}
	class I @JvmOverloads constructor(var kI: Double, var lowerLimit: Double = Double.NEGATIVE_INFINITY, var upperLimit: Double = Double.POSITIVE_INFINITY) : ControllerCalculation<Double> {
		var i = 0.0
		override fun evaluate(
			accumulation: Double,
			currentState: Double,
			target: Double,
			error: Double,
			deltaTime: Double
		): Double {
			i += (error / deltaTime) * kI
			i = i.coerceIn(lowerLimit, upperLimit)
			return accumulation + i
		}
	}
	class D (var kD: Double) : ControllerCalculation<Double> {
		private var previousError = 0.0

		override fun evaluate(
			accumulation: Double,
			currentState: Double,
			target: Double,
			error: Double,
			deltaTime: Double
		): Double {
			val result = ((error - previousError) / deltaTime) * kD
			previousError = error
			return accumulation + result
		}
	}
}