package dev.frozenmilk.dairy.core.util.current

import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit
import java.util.function.Supplier
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * common unit is [CurrentUnits.AMP]
 */
interface CurrentUnit : Unit<CurrentUnit>

enum class CurrentUnits(val sdkUnit: org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit, override val toCommonRatio: Double) : CurrentUnit {
	AMP(org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.AMPS, 1.0),
	MILLI_AMP(org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.MILLIAMPS, 0.001);

	companion object {
		val sdkMap = mapOf(org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.AMPS to AMP, org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit.MILLIAMPS to MILLI_AMP)
	}
}

class Current(unit: CurrentUnit, value: Double = 0.0) : ReifiedUnit<CurrentUnit, Current>(unit, value) {
	constructor(sdkUnit: org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit, value: Double = 0.0) : this(CurrentUnits.sdkMap[sdkUnit]!!, value)
	fun into(sdkUnit: org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit) = into(CurrentUnits.sdkMap[sdkUnit]!!)
	override fun into(unit: CurrentUnit) = if (unit == this.unit) this else Current(unit, this.unit.into(unit, value))
	override fun plus(reifiedUnit: Current) = Current(unit, value + reifiedUnit[unit])
	override fun minus(reifiedUnit: Current) = Current(unit, value - reifiedUnit[unit])
	override fun unaryPlus() = this
	override fun unaryMinus() = Current(unit, -value)
	override fun times(multiplier: Double) = Current(unit, value * multiplier)
	override fun times(multiplier: Current) = Current(unit, value * multiplier[unit])
	override fun div(divisor: Double) = Current(unit, value / divisor)
	override fun div(divisor: Current) = Current(unit, value / divisor[unit])
	override fun pow(n: Double) = Current(unit, value.pow(n))
	override fun pow(n: Int) = Current(unit, value.pow(n))
	override fun sqrt() = Current(unit, sqrt(value))
	override fun abs() = Current(unit, abs(value))
	override fun findError(target: Current) = Current(unit, target[unit] - value)
	override fun coerceAtLeast(minimumValue: Current) = Current(unit, value.coerceAtLeast(minimumValue[unit]))
	override fun coerceAtMost(maximumValue: Current) = Current(unit, value.coerceAtMost(maximumValue[unit]))
	override fun coerceIn(minimumValue: Current, maximumValue: Current) = Current(unit, value.coerceIn(minimumValue[unit], maximumValue[unit]))
	override fun compareTo(other: Current): Int = value.compareTo(other[unit]) // ignores power
	override fun toString() = "$value $unit"
	override fun equals(other: Any?): Boolean = other is Current && abs((this - other).value) < 1e-12
	override fun hashCode(): Int = intoAmps().value.hashCode() // ignores power

	companion object {
		@JvmField
		val NEGATIVE_INFINITY: Current = Current(CurrentUnits.AMP, Double.NEGATIVE_INFINITY)
		@JvmField
		val POSITIVE_INFINITY: Current = Current(CurrentUnits.AMP, Double.POSITIVE_INFINITY)
		@JvmField
		val NaN: Current = Current(CurrentUnits.AMP, Double.NaN)
	}

	// quick intos
	fun intoAmps() = into(CurrentUnits.AMP)
	fun intoMilliAmps() = into(CurrentUnits.MILLI_AMP)
}
// quick intos
fun Supplier<out Current>.into(unit: CurrentUnit) = Supplier { get().into(unit) }
fun Supplier<out Current>.intoAmps() = Supplier { get().intoAmps() }
fun Supplier<out Current>.intoMilliAmps() = Supplier { get().intoMilliAmps() }
