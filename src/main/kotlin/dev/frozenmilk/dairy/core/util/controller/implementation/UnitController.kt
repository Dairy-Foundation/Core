package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.controller.Controller
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.logical.Conditional
import dev.frozenmilk.dairy.core.util.supplier.numeric.CachedMotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedComparableNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import java.util.function.Consumer

/**
 * [deregister]s at the end of the OpMode,
 * it is recommended to either re-[register] if you wish to re-use this,
 * or regenerate for each OpMode (which must be done if this is built from regenerated resourced, like motors or encoders)
 */
@Suppress("INAPPLICABLE_JVM_NAME")
class UnitController<RU: ReifiedUnit<*, RU>> : Controller<RU>, EnhancedComparableNumericSupplier<RU, Conditional<RU>>{
	/**
	 * @param targetSupplier supplier for the target position
	 * @param stateSupplier supplier for the system state
	 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [state]
	 */
	@JvmOverloads
	constructor(
		targetSupplier: MotionComponentSupplier<out RU>,
		stateSupplier: MotionComponentSupplier<out RU>,
		toleranceEpsilon: MotionComponentSupplier<out RU>,
		outputConsumer: MotionComponentConsumer<RU> = MotionComponentConsumer {},
		controllerCalculation: ControllerCalculation<RU>,
	) : super(
		targetSupplier,
		stateSupplier,
		{ targetSupplier, stateSupplier, motionComponent ->
			targetSupplier[motionComponent].findError(stateSupplier[motionComponent])
		},
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation,
	)
	/**
	 * @param targetSupplier supplier for the target position
	 * @param stateSupplier supplier for the system state
	 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [state]
	 */
	constructor(
		targetSupplier: MotionComponentSupplier<out RU>,
		stateSupplier: MotionComponentSupplier<out RU>,
		toleranceEpsilon: MotionComponentSupplier<out RU>,
		outputConsumer: Consumer<in RU>,
		controllerCalculation: ControllerCalculation<RU>,
	) : super(
		targetSupplier,
		stateSupplier,
		{ targetSupplier, stateSupplier, motionComponent ->
			targetSupplier[motionComponent].findError(stateSupplier[motionComponent])
		},
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation,
	)

	//
	// Controller
	//
	override fun finished(toleranceEpsilon: MotionComponentSupplier<out RU>) =
		MotionComponents.entries.all {
			val error = errorSupplier.get(it).absoluteValue
			val tolerance = toleranceEpsilon.get(it)
			error.isNaN() || tolerance.isNaN() || error <= tolerance
		}

	//
	// EnhancedNumericSupplier
	//
	override val zero = stateSupplier[MotionComponents.STATE].let { it - it }
	private var offset = zero
	@get:JvmName("state")
	@set:JvmName("state")
	override var state: RU
		get() = get() - offset
		set(value) {
			offset = currentState - value
		}
	@get:JvmName("velocity")
	override val velocity get() = previousPositions.homogenise().getVelocity()
	@get:JvmName("rawVelocity")
	override val rawVelocity get() = previousPositions.last().getVelocity()
	@get:JvmName("acceleration")
	override val acceleration get() = previousVelocities.homogenise().getVelocity()
	@get:JvmName("rawAcceleration")
	override val rawAcceleration get() = previousVelocities.last().getVelocity()

	//
	// Comparable
	//
	override fun conditionalBindState() = Conditional(this::state)
	override fun conditionalBindVelocity() = Conditional(this::velocity)
	override fun conditionalBindVelocityRaw() = Conditional(this::rawVelocity)
	override fun conditionalBindAcceleration() = Conditional(this::acceleration)
	override fun conditionalBindAccelerationRaw() = Conditional(this::rawAcceleration)
}