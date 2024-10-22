package dev.frozenmilk.dairy.core.util.controller.calculation.pid

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.angle.Angle
import kotlin.math.cos
import kotlin.math.sign

abstract class UnitComponent private constructor() {
	class P <RU: ReifiedUnit<*, RU>> (val motionComponent: MotionComponents, var kP: Double) : ControllerCalculation<RU> {
		override fun update(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		): RU {
			val res = error.get(motionComponent) * kP
			return if (res.isNaN()) accumulation
			else accumulation + res
		}

		override fun reset() {}
	}
	class SqrtP <RU: ReifiedUnit<*, RU>> (val motionComponent: MotionComponents, var kSqrtP: Double) : ControllerCalculation<RU> {
		override fun update(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		): RU {
			val err = error[motionComponent]
			val res = err.intoCommon().absoluteValue.sqrt() * err.sign * kSqrtP
			return if (res.isNaN()) accumulation
			else accumulation + res
		}

		override fun reset() {}
	}
	class I <RU: ReifiedUnit<*, RU>> @JvmOverloads constructor(val motionComponent: MotionComponents, var kI: Double, var lowerLimit: RU? = null, var upperLimit: RU? = null) : ControllerCalculation<RU> {
		var i: RU? = null
		override fun update(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		) {
			var iLocal = i;
			if (iLocal == null) iLocal = accumulation - accumulation
			iLocal += (error.get(motionComponent).intoCommon() / deltaTime) * kI
			if (lowerLimit != null) iLocal = iLocal.coerceAtLeast(lowerLimit!!)
			if (upperLimit != null) iLocal = iLocal.coerceAtMost(upperLimit!!)
			i = iLocal
		}

		override fun evaluate(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		): RU {
			update(accumulation, state, target, error, deltaTime)
			val iLocal = i
			return if (iLocal?.isNaN() != false) accumulation
			else accumulation + iLocal
		}

		override fun reset() {
			i = null
		}
	}
	class D <RU: ReifiedUnit<*, RU>> (val motionComponent: MotionComponents, var kD: Double) : ControllerCalculation<RU> {
		private var previousError: RU? = null
		override fun update(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		) {
			previousError = error.get(motionComponent).intoCommon()
		}

		override fun evaluate(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		): RU {
			var prev = previousError
			if (prev == null) prev = accumulation - accumulation
			val err = error.get(motionComponent).intoCommon()
			val res = ((err - prev) / deltaTime) * kD
			previousError = err
			return if (res.isNaN()) accumulation
			else accumulation + res
		}

		override fun reset() {
			previousError = null
		}
	}

	class FF <RU: ReifiedUnit<*, RU>> (val motionComponent: MotionComponents, var kFF: Double) : ControllerCalculation<RU> {
		override fun update(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		): RU {
			val res = target[motionComponent] * kFF
			return if (res.isNaN()) accumulation
			else accumulation + res
		}

		override fun reset() {}
	}
	class SignFF <RU: ReifiedUnit<*, RU>> (val motionComponent: MotionComponents, var kFF: RU) : ControllerCalculation<RU> {
		override fun update(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		): RU {
			val res = kFF * target[motionComponent].sign
			return if (res.isNaN()) accumulation
			else accumulation + res
		}

		override fun reset() {}
	}
	class StateCosFF (val motionComponent: MotionComponents, var kFF: Angle) : ControllerCalculation<Angle> {
		override fun update(
			accumulation: Angle,
			state: MotionComponentSupplier<out Angle>,
			target: MotionComponentSupplier<out Angle>,
			error: MotionComponentSupplier<out Angle>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: Angle,
			state: MotionComponentSupplier<out Angle>,
			target: MotionComponentSupplier<out Angle>,
			error: MotionComponentSupplier<out Angle>,
			deltaTime: Double
		): Angle {
			val res = kFF * state[motionComponent].cos
			return if (res.isNaN()) accumulation
			else accumulation + res
		}

		override fun reset() {}
	}
	class TargetCosFF (val motionComponent: MotionComponents, var kFF: Angle) : ControllerCalculation<Angle> {
		override fun update(
			accumulation: Angle,
			state: MotionComponentSupplier<out Angle>,
			target: MotionComponentSupplier<out Angle>,
			error: MotionComponentSupplier<out Angle>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: Angle,
			state: MotionComponentSupplier<out Angle>,
			target: MotionComponentSupplier<out Angle>,
			error: MotionComponentSupplier<out Angle>,
			deltaTime: Double
		): Angle {
			val res = kFF * target[motionComponent].cos
			return if (res.isNaN()) accumulation
			else accumulation + res
		}

		override fun reset() {}
	}

	class Constant <RU: ReifiedUnit<*, RU>> (var k: RU) : ControllerCalculation<RU> {
		override fun update(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		) {}

		override fun evaluate(
			accumulation: RU,
			state: MotionComponentSupplier<out RU>,
			target: MotionComponentSupplier<out RU>,
			error: MotionComponentSupplier<out RU>,
			deltaTime: Double
		): RU {
			return if (k.isNaN()) accumulation
			else accumulation + k
		}

		override fun reset() {}
	}
}