package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.controller.Controller
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.util.supplier.numeric.positional.EnhancedPose2DSupplier
import dev.frozenmilk.util.units.position.Pose2D
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * @param targetSupplier supplier for the target position
 * @param inputSupplier supplier for the system state
 * @param motionComponent motionComponent that this controller will act on
 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
 * @param outputConsumer method to update the output consumer of the system
 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [output]
 *
 * [deregister]s at the end of the OpMode,
 * it is recommended to either re-[register] if you wish to re-use this,
 * or regenerate for each OpMode (which must be done if this is built from regenerated resourced, like motors or encoders)
 */
class PoseController(
	targetSupplier: Supplier<out Pose2D>,
	inputSupplier: IEnhancedNumericSupplier<Pose2D>,
	motionComponent: MotionComponents,
	toleranceEpsilon: Pose2D,
	outputConsumer: Consumer<Pose2D> = Consumer {},
	controllerCalculation: ControllerCalculation<Pose2D>
) : Controller<Pose2D>(
	targetSupplier,
	inputSupplier,
	motionComponent,
	toleranceEpsilon,
	outputConsumer,
	controllerCalculation
) {
	/**
	 * @param target target position
	 * @param inputSupplier supplier for the system state
	 * @param motionComponent motionComponent that this controller will act on
	 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [output]
	 */
	@JvmOverloads
	constructor(
		target: Pose2D,
		inputSupplier: IEnhancedNumericSupplier<Pose2D>,
		motionComponent: MotionComponents,
		toleranceEpsilon: Pose2D,
		outputConsumer: Consumer<Pose2D> = Consumer {},
		controllerCalculation: ControllerCalculation<Pose2D>,
	) : this (Supplier { target }, inputSupplier, motionComponent, toleranceEpsilon, outputConsumer, controllerCalculation)

	override val zero = Pose2D()
	override val supplier by lazy { EnhancedPose2DSupplier(this::output) }
	override fun finished(toleranceEpsilon: Pose2D) = error().vector2D.magnitude.abs() <= toleranceEpsilon.vector2D.magnitude.abs() && error().heading.abs() <= toleranceEpsilon.heading
}