package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.controller.Controller
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.CachedMotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import dev.frozenmilk.util.units.position.DistanceVector2D
import java.util.function.Consumer

/**
 * [deregister]s at the end of the OpMode,
 * it is recommended to either re-[register] if you wish to re-use this,
 * or regenerate for each OpMode (which must be done if this is built from regenerated resourced, like motors or encoders)
 */
@Suppress("INAPPLICABLE_JVM_NAME")
class DistanceVectorController : Controller<DistanceVector2D> {
	/**
	 * @param targetSupplier supplier for the target position
	 * @param stateSupplier supplier for the system state
	 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [state]
	 */
	@JvmOverloads
	constructor(
		targetSupplier: MotionComponentSupplier<out DistanceVector2D>,
		stateSupplier: MotionComponentSupplier<out DistanceVector2D>,
		toleranceEpsilon: MotionComponentSupplier<out DistanceVector2D>,
		outputConsumer: MotionComponentConsumer<DistanceVector2D> = MotionComponentConsumer {},
		controllerCalculation: ControllerCalculation<DistanceVector2D>,
	) : super(
		targetSupplier,
		stateSupplier,
		CachedMotionComponentSupplier {
			targetSupplier[it] - stateSupplier[it]
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
		targetSupplier: MotionComponentSupplier<out DistanceVector2D>,
		stateSupplier: MotionComponentSupplier<out DistanceVector2D>,
		toleranceEpsilon: MotionComponentSupplier<out DistanceVector2D>,
		outputConsumer: Consumer<in DistanceVector2D>,
		controllerCalculation: ControllerCalculation<DistanceVector2D>,
	) : super(
		targetSupplier,
		stateSupplier,
		CachedMotionComponentSupplier {
			targetSupplier[it] - stateSupplier[it]
		},
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation,
	)

	//
	// Controller
	//
	override fun finished(toleranceEpsilon: MotionComponentSupplier<out DistanceVector2D>) =
		MotionComponents.entries.all {
			val error = errorSupplier[it].magnitude
			val tolerance = toleranceEpsilon[it].magnitude
			error.isNaN() || tolerance.isNaN() || error.absoluteValue <= tolerance
		}

	//
	// EnhancedNumericSupplier
	//
	override val zero = DistanceVector2D()
	private var offset = zero
	@get:JvmName("state")
	@set:JvmName("state")
	override var state: DistanceVector2D
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
