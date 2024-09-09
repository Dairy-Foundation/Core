package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.controller.Controller
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.CachedMotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import dev.frozenmilk.util.units.position.DistancePose2D
import java.util.function.Consumer

/**
 * [deregister]s at the end of the OpMode,
 * it is recommended to either re-[register] if you wish to re-use this,
 * or regenerate for each OpMode (which must be done if this is built from regenerated resourced, like motors or encoders)
 */
@Suppress("INAPPLICABLE_JVM_NAME")
class DistancePoseController : Controller<DistancePose2D> {
	/**
	 * @param targetSupplier supplier for the target position
	 * @param stateSupplier supplier for the system state
	 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [state]
	 */
	@JvmOverloads
	constructor(
		targetSupplier: MotionComponentSupplier<out DistancePose2D>,
		stateSupplier: MotionComponentSupplier<out DistancePose2D>,
		toleranceEpsilon: MotionComponentSupplier<out DistancePose2D>,
		outputConsumer: MotionComponentConsumer<DistancePose2D> = MotionComponentConsumer {},
		controllerCalculation: ControllerCalculation<DistancePose2D>,
	) : super(
		targetSupplier,
		stateSupplier,
		CachedMotionComponentSupplier {
			targetSupplier.get(it) - stateSupplier.get(it)
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
		targetSupplier: MotionComponentSupplier<out DistancePose2D>,
		stateSupplier: MotionComponentSupplier<out DistancePose2D>,
		toleranceEpsilon: MotionComponentSupplier<out DistancePose2D>,
		outputConsumer: Consumer<in DistancePose2D>,
		controllerCalculation: ControllerCalculation<DistancePose2D>,
	) : super(
		targetSupplier,
		stateSupplier,
		CachedMotionComponentSupplier {
			val (tV, tA) = targetSupplier.get(it)
			val (sV, sA) = stateSupplier.get(it)
			DistancePose2D(tV - sV, tA.findError(sA))
		},
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation,
	)

	//
	// Controller
	//
	override fun finished(toleranceEpsilon: MotionComponentSupplier<out DistancePose2D>) =
		MotionComponents.entries.all {
			val (eVec, eTheta) = errorSupplier[it]
			val (tVec, tTheta) = toleranceEpsilon[it]
			val eMag = eVec.magnitude
			val tMag = tVec.magnitude
			val vecFin = eMag.isNaN() || tMag.isNaN() || eMag <= tMag
			val thetaFin = eTheta.isNaN() || tTheta.isNaN() || eTheta.absoluteValue <= tTheta
			vecFin && thetaFin
		}

	//
	// EnhancedNumericSupplier
	//
	override val zero = DistancePose2D()
	private var offset = zero
	@get:JvmName("state")
	@set:JvmName("state")
	override var state: DistancePose2D
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
