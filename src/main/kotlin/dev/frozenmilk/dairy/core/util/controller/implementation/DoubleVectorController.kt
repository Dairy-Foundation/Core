package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.controller.Controller
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.CachedMotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import dev.frozenmilk.util.units.position.DoubleVector2D
import java.util.function.Consumer
import kotlin.math.absoluteValue

/**
 * [deregister]s at the end of the OpMode,
 * it is recommended to either re-[register] if you wish to re-use this,
 * or regenerate for each OpMode (which must be done if this is built from regenerated resourced, like motors or encoders)
 */
@Suppress("INAPPLICABLE_JVM_NAME")
class DoubleVectorController : Controller<DoubleVector2D> {
	/**
	 * @param targetSupplier supplier for the target position
	 * @param stateSupplier supplier for the system state
	 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [state]
	 */
	@JvmOverloads
	constructor(
		targetSupplier: MotionComponentSupplier<out DoubleVector2D>,
		stateSupplier: MotionComponentSupplier<out DoubleVector2D>,
		toleranceEpsilon: MotionComponentSupplier<out DoubleVector2D>,
		outputConsumer: MotionComponentConsumer<DoubleVector2D> = MotionComponentConsumer {},
		controllerCalculation: ControllerCalculation<DoubleVector2D>,
	) : super(
		targetSupplier,
		stateSupplier,
		{ targetSupplier, stateSupplier, motionComponent ->
			targetSupplier[motionComponent] - stateSupplier[motionComponent]
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
		targetSupplier: MotionComponentSupplier<out DoubleVector2D>,
		stateSupplier: MotionComponentSupplier<out DoubleVector2D>,
		toleranceEpsilon: MotionComponentSupplier<out DoubleVector2D>,
		outputConsumer: Consumer<in DoubleVector2D>,
		controllerCalculation: ControllerCalculation<DoubleVector2D>,
	) : super(
		targetSupplier,
		stateSupplier,
		{ targetSupplier, stateSupplier, motionComponent ->
			targetSupplier[motionComponent] - stateSupplier[motionComponent]
		},
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation,
	)

	//
	// Controller
	//
	override fun finished(toleranceEpsilon: MotionComponentSupplier<out DoubleVector2D>) =
		MotionComponents.entries.all {
			val error = errorSupplier[it].magnitude
			val tolerance = toleranceEpsilon[it].magnitude
			error.isNaN() || tolerance.isNaN() || error.absoluteValue <= tolerance
		}

	//
	// EnhancedNumericSupplier
	//
	override val zero = DoubleVector2D()
	private var offset = zero
	@get:JvmName("state")
	@set:JvmName("state")
	override var state: DoubleVector2D
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
}
