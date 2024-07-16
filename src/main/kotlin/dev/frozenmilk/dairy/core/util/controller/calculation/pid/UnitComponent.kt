package dev.frozenmilk.dairy.core.util.controller.calculation.pid

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit

class UnitComponent {
	class P <U: Unit<U>, RU: ReifiedUnit<U, RU>> (var kP: RU) : ControllerCalculation<RU> {
		override fun evaluate(
			accumulation: RU,
			currentState: RU,
			target: RU,
			error: RU,
			deltaTime: Double
		) = accumulation + (error * kP)
	}
	class I <U: Unit<U>, RU: ReifiedUnit<U, RU>> @JvmOverloads constructor(var kI: RU, var lowerLimit: RU? = null, var upperLimit: RU? = null) : ControllerCalculation<RU> {
		var i: RU? = null
		override fun evaluate(
			accumulation: RU,
			currentState: RU,
			target: RU,
			error: RU,
			deltaTime: Double
		): RU {
			if (i == null) i = currentState - currentState
			i = i!! + (error.intoCommon() / deltaTime) * kI
			if (lowerLimit != null) i = i!!.coerceAtLeast(lowerLimit!!)
			if (upperLimit != null) i = i!!.coerceAtMost(upperLimit!!)
			return accumulation + i!!
		}
	}
	class D <U: Unit<U>, RU: ReifiedUnit<U, RU>> (var kD: RU) : ControllerCalculation<RU> {
		private var previousError: RU? = null

		override fun evaluate(
			accumulation: RU,
			currentState: RU,
			target: RU,
			error: RU,
			deltaTime: Double
		): RU {
			if (previousError == null) previousError = currentState - currentState
			val result = ((error.intoCommon() - previousError!!) / deltaTime) * kD
			previousError = error
			return accumulation + result
		}
	}
}