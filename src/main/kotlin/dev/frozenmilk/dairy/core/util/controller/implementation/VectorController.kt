package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.controller.Controller
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.util.supplier.numeric.positional.EnhancedVector2DSupplier
import dev.frozenmilk.util.modifier.Modifier
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import dev.frozenmilk.util.units.position.Vector2D
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * [deregister]s at the end of the OpMode,
 * it is recommended to either re-[register] if you wish to re-use this,
 * or regenerate for each OpMode (which must be done if this is built from regenerated resourced, like motors or encoders)
 */
class VectorController
/**
 * @param targetSupplier supplier for the target position
 * @param inputSupplier supplier for the system state
 * @param motionComponent motionComponent that this controller will act on
 * @param toleranceEpsilon used for [finished], magnitudinal tolerance for determining if the system is finished
 * @param outputConsumer method to update the output consumer of the system
 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [position]
 * @param modifier modifying function to apply to the result of [controllerCalculation]
 */
@JvmOverloads constructor(
	targetSupplier: MotionComponentSupplier<out Vector2D>,
	inputSupplier: MotionComponentSupplier<Vector2D>,
	motionComponent: MotionComponents,
	toleranceEpsilon: Vector2D,
	outputConsumer: Consumer<Vector2D> = Consumer {},
	controllerCalculation: ControllerCalculation<Vector2D>,
	modifier: Modifier<Vector2D> = Modifier { it }
) : Controller<Vector2D>(
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
	 * @param toleranceEpsilon used for [finished], magnitudinal tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [position]
	 * @param modifier modifying function to apply to the result of [controllerCalculation]
	 */
	@JvmOverloads constructor(
		targetSupplier: Supplier<out Vector2D>,
		inputSupplier: MotionComponentSupplier<Vector2D>,
		motionComponent: MotionComponents,
		toleranceEpsilon: Vector2D,
		outputConsumer: Consumer<Vector2D> = Consumer {},
		controllerCalculation: ControllerCalculation<Vector2D>,
		modifier: Modifier<Vector2D> = Modifier { it }
	) : this(
		object : MotionComponentSupplier<Vector2D> {
			override fun component(motionComponent: MotionComponents) = targetSupplier.get()
			override fun componentError(motionComponent: MotionComponents, target: Vector2D) = target - targetSupplier.get()
		}, inputSupplier, motionComponent, toleranceEpsilon, outputConsumer, controllerCalculation, modifier
	)
	/**
	 * @param target target position
	 * @param inputSupplier supplier for the system state
	 * @param motionComponent motionComponent that this controller will act on
	 * @param toleranceEpsilon used for [finished], magnitudinal tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [position]
	 * @param modifier modifying function to apply to the result of [controllerCalculation]
	 */
	@JvmOverloads
	constructor(
		target: Vector2D,
		inputSupplier: MotionComponentSupplier<Vector2D>,
		motionComponent: MotionComponents,
		toleranceEpsilon: Vector2D,
		outputConsumer: Consumer<Vector2D> = Consumer {},
		controllerCalculation: ControllerCalculation<Vector2D>,
		modifier: Modifier<Vector2D> = Modifier { it }
	) : this (Supplier { target }, inputSupplier, motionComponent, toleranceEpsilon, outputConsumer, controllerCalculation, modifier)

	//
	// Controller
	//
	override var target: Vector2D
		get() = targetSupplier.component(motionComponent)
		set(value) {
			targetSupplier = object : MotionComponentSupplier<Vector2D> {
				override fun component(motionComponent: MotionComponents) = value
				override fun componentError(
					motionComponent: MotionComponents,
					target: Vector2D
				) = target - value
			}
		}
	override fun finished(toleranceEpsilon: Vector2D) = inputSupplier.componentError(motionComponent, target).magnitude <= toleranceEpsilon.magnitude

	//
	// EnhancedNumericSupplier
	//
	override val zero = Vector2D()
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
	override fun findErrorPosition(target: Vector2D) = target - position
	override fun findErrorVelocity(target: Vector2D) = target - velocity
	override fun findErrorRawVelocity(target: Vector2D) = target - rawVelocity
	override fun findErrorAcceleration(target: Vector2D) = target - acceleration
	override fun findErrorRawAcceleration(target: Vector2D) = target - rawAcceleration
	override fun <N2> merge(supplier: Supplier<out N2>, merge: (Vector2D, N2) -> Vector2D) = EnhancedVector2DSupplier({ merge(get(), supplier.get()) }, modifier)
	override fun applyModifier(modifier: Modifier<Vector2D>) = VectorController(
		targetSupplier,
		inputSupplier,
		motionComponent,
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation
	) { modifier.modify(this.modifier.modify(it)) }
	override fun setModifier(modifier: Modifier<Vector2D>) = VectorController(
		targetSupplier,
		inputSupplier,
		motionComponent,
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation,
		modifier
	)
}