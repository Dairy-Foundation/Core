package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.controller.Controller
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.CachedMotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import dev.frozenmilk.util.units.position.DoublePose2D
import java.util.function.Consumer

/**
 * [deregister]s at the end of the OpMode,
 * it is recommended to either re-[register] if you wish to re-use this,
 * or regenerate for each OpMode (which must be done if this is built from regenerated resourced, like motors or encoders)
 */
@Suppress("INAPPLICABLE_JVM_NAME")
class DoublePoseController : Controller<DoublePose2D> {
	/**
	 * @param targetSupplier supplier for the target position
	 * @param stateSupplier supplier for the system state
	 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [state]
	 */
	@JvmOverloads
	constructor(
		targetSupplier: MotionComponentSupplier<out DoublePose2D>,
		stateSupplier: MotionComponentSupplier<out DoublePose2D>,
		toleranceEpsilon: MotionComponentSupplier<out DoublePose2D>,
		outputConsumer: MotionComponentConsumer<DoublePose2D> = MotionComponentConsumer {},
		controllerCalculation: ControllerCalculation<DoublePose2D>,
	) : super(
		targetSupplier,
		stateSupplier,
		{ targetSupplier, stateSupplier, motionComponent ->
			val (tV, tA) = targetSupplier[motionComponent]
			val (sV, sA) = stateSupplier[motionComponent]
			DoublePose2D(tV - sV, tA.findError(sA))
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
		targetSupplier: MotionComponentSupplier<out DoublePose2D>,
		stateSupplier: MotionComponentSupplier<out DoublePose2D>,
		toleranceEpsilon: MotionComponentSupplier<out DoublePose2D>,
		outputConsumer: Consumer<in DoublePose2D>,
		controllerCalculation: ControllerCalculation<DoublePose2D>,
	) : super(
		targetSupplier,
		stateSupplier,
		{ targetSupplier, stateSupplier, motionComponent ->
			val (tV, tA) = targetSupplier[motionComponent]
			val (sV, sA) = stateSupplier[motionComponent]
			DoublePose2D(tV - sV, tA.findError(sA))
		},
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation,
	)

	//
	// Controller
	//
	override fun finished(toleranceEpsilon: MotionComponentSupplier<out DoublePose2D>) =
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
	override val zero = DoublePose2D()
	private var offset = zero
	@get:JvmName("state")
	@set:JvmName("state")
	override var state: DoublePose2D
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
