package dev.frozenmilk.dairy.core.util.controller.calculation.pid

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sign
import kotlin.math.sqrt

abstract class DoubleComponent private constructor() {
	class P (val motionComponent: MotionComponents, var kP: Double) : ControllerCalculation<Double> {
		override fun update(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		): Double {
			val res = error.get(motionComponent) * kP
			return if (res.isNaN()) accumulation
			else accumulation + res
		}

		override fun reset() {}
	}
	class SqrtP (val motionComponent: MotionComponents, var kSqrtP: Double) : ControllerCalculation<Double> {
		override fun update(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		): Double {
			val err = error[motionComponent]
			val res = sqrt(err.absoluteValue) * err.sign * kSqrtP
			return if (res.isNaN()) accumulation
			else accumulation + res
		}

		override fun reset() {}
	}
	class I @JvmOverloads constructor(val motionComponent: MotionComponents, var kI: Double, var lowerLimit: Double = Double.NEGATIVE_INFINITY, var upperLimit: Double = Double.POSITIVE_INFINITY) : ControllerCalculation<Double> {
		var i = 0.0
		override fun update(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		) {
			i += (error.get(motionComponent) / deltaTime) * kI
			i = i.coerceIn(lowerLimit, upperLimit)
		}

		override fun evaluate(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		): Double {
			update(accumulation, state, target, error, deltaTime)
			return if (i.isNaN()) accumulation
			else accumulation + i
		}

		override fun reset() {
			i = 0.0
		}
	}
	class D (val motionComponent: MotionComponents, var kD: Double) : ControllerCalculation<Double> {
		private var previousError = 0.0
		override fun update(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		) {
			previousError = error.get(motionComponent)
		}

		override fun evaluate(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		): Double {
			val err = error.get(motionComponent);
			val res = ((err - previousError) / deltaTime) * kD
			previousError = err
			if (res.isNaN()) return accumulation
			return accumulation + res
		}

		override fun reset() {
			previousError = 0.0
		}
	}

	class FF (val motionComponent: MotionComponents, var kFF: Double) : ControllerCalculation<Double> {
		override fun update(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		): Double {
			val res = target[motionComponent] * kFF
			return if (res.isNaN()) accumulation
			else accumulation + res
		}

		override fun reset() {}
	}
	class SignFF (val motionComponent: MotionComponents, var kFF: Double) : ControllerCalculation<Double> {
		override fun update(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		): Double {
			val res = target[motionComponent].sign * kFF
			return if (res.isNaN()) accumulation
			else accumulation + res
		}

		override fun reset() {}
	}
	class CosFF (val motionComponent: MotionComponents, var kFF: Double) : ControllerCalculation<Double> {
		override fun update(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		): Double {
			val res = cos(target[motionComponent]) * kFF
			return if (res.isNaN()) accumulation
			else accumulation + res
		}

		override fun reset() {}
	}

	class Constant (var k: Double) : ControllerCalculation<Double> {
		override fun update(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: Double,
			state: MotionComponentSupplier<out Double>,
			target: MotionComponentSupplier<out Double>,
			error: MotionComponentSupplier<out Double>,
			deltaTime: Double
		): Double {
			return if (k.isNaN()) accumulation
			else accumulation + k
		}

		override fun reset() {}
	}
}