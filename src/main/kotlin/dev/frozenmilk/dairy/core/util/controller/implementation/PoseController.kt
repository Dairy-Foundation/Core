package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.controller.Controller
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.util.supplier.numeric.positional.EnhancedPose2DSupplier
import dev.frozenmilk.util.modifier.Modifier
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import dev.frozenmilk.util.units.position.Pose2D
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * [deregister]s at the end of the OpMode,
 * it is recommended to either re-[register] if you wish to re-use this,
 * or regenerate for each OpMode (which must be done if this is built from regenerated resourced, like motors or encoders)
 */
class PoseController
/**
 * @param targetSupplier supplier for the target position
 * @param inputSupplier supplier for the system state
 * @param motionComponent motionComponent that this controller will act on
 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
 * @param outputConsumer method to update the output consumer of the system
 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [position]
 * @param modifier modifying function to apply to the result of [controllerCalculation]
 */
@JvmOverloads
constructor(
	targetSupplier: MotionComponentSupplier<out Pose2D>,
	inputSupplier: MotionComponentSupplier<Pose2D>,
	motionComponent: MotionComponents,
	toleranceEpsilon: Pose2D,
	outputConsumer: Consumer<Pose2D> = Consumer {},
	controllerCalculation: ControllerCalculation<Pose2D>,
	modifier: Modifier<Pose2D> = Modifier { it }
) : Controller<Pose2D>(
	targetSupplier,
	inputSupplier,
	motionComponent,
	toleranceEpsilon,
	outputConsumer,
	controllerCalculation,
	modifier
) {
	/**
	 * @param targetSupplier supplier for the target position
	 * @param inputSupplier supplier for the system state
	 * @param motionComponent motionComponent that this controller will act on
	 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [position]
	 * @param modifier modifying function to apply to the result of [controllerCalculation]
	 */
	@JvmOverloads
	constructor(
		targetSupplier: Supplier<out Pose2D>,
		inputSupplier: MotionComponentSupplier<Pose2D>,
		motionComponent: MotionComponents,
		toleranceEpsilon: Pose2D,
		outputConsumer: Consumer<Pose2D> = Consumer {},
		controllerCalculation: ControllerCalculation<Pose2D>,
		modifier: Modifier<Pose2D> = Modifier { it }
	) : this(
		object : MotionComponentSupplier<Pose2D> { // falsify the information
			override fun component(motionComponent: MotionComponents) = targetSupplier.get()
			override fun componentError(motionComponent: MotionComponents, target: Pose2D) = target - targetSupplier.get()
		},
		inputSupplier, motionComponent, toleranceEpsilon, outputConsumer, controllerCalculation, modifier
	)
	/**
	 * @param target target position
	 * @param inputSupplier supplier for the system state
	 * @param motionComponent motionComponent that this controller will act on
	 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [position]
	 * @param modifier modifying function to apply to the result of [controllerCalculation]
	 */
	@JvmOverloads
	constructor(
		target: Pose2D,
		inputSupplier: IEnhancedNumericSupplier<Pose2D>,
		motionComponent: MotionComponents,
		toleranceEpsilon: Pose2D,
		outputConsumer: Consumer<Pose2D> = Consumer {},
		controllerCalculation: ControllerCalculation<Pose2D>,
		modifier: Modifier<Pose2D> = Modifier { it }
	) : this (Supplier { target }, inputSupplier, motionComponent, toleranceEpsilon, outputConsumer, controllerCalculation, modifier)

	//
	// Controller
	//
	override var target: Pose2D
		get() = targetSupplier.component(motionComponent)
		set(value) {
			targetSupplier = object : MotionComponentSupplier<Pose2D> {
				override fun component(motionComponent: MotionComponents) = value
				override fun componentError(
					motionComponent: MotionComponents,
					target: Pose2D
				) = target - value
			}
		}
	override fun finished(toleranceEpsilon: Pose2D) = inputSupplier.componentError(motionComponent, target).run {
		vector2D.magnitude.abs() <= toleranceEpsilon.vector2D.magnitude.abs() && heading.abs() <= toleranceEpsilon.heading
	}


	//
	// EnhancedNumericSupplier
	//
	override val zero = Pose2D()
	private var offset = zero
	override var position
		get() = get() - offset
		set(value) {
			offset = current - value
		}
	override val velocity get() = previousPositions.homogenise().getVelocity()
	override val rawVelocity get() = previousPositions.last().getVelocity()
	override val acceleration get() = previousVelocities.homogenise().getVelocity()
	override val rawAcceleration get() = previousVelocities.last().getVelocity()
	override fun findErrorPosition(target: Pose2D) = target - position
	override fun findErrorVelocity(target: Pose2D) = target - velocity
	override fun findErrorRawVelocity(target: Pose2D) = target - rawVelocity
	override fun findErrorAcceleration(target: Pose2D) = target - acceleration
	override fun findErrorRawAcceleration(target: Pose2D) = target - rawAcceleration
	override fun <N2> merge(supplier: Supplier<out N2>, merge: (Pose2D, N2) -> Pose2D) = EnhancedPose2DSupplier({ merge(get(), supplier.get()) }, modifier)
	override fun applyModifier(modifier: Modifier<Pose2D>) = PoseController(
		targetSupplier,
		inputSupplier,
		motionComponent,
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation
	) { modifier.modify(this.modifier.modify(it)) }
	override fun setModifier(modifier: Modifier<Pose2D>) = PoseController(
		targetSupplier,
		inputSupplier,
		motionComponent,
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation,
		modifier
	)
}