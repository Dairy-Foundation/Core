package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.controller.Controller
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.logical.Conditional
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedComparableNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedDoubleSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.util.modifier.Modifier
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.math.abs

/**
 * [deregister]s at the end of the OpMode,
 * it is recommended to either re-[register] if you wish to re-use this,
 * or regenerate for each OpMode (which must be done if this is built from regenerated resourced, like motors or encoders)
 */
class DoubleController
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
	targetSupplier: MotionComponentSupplier<out Double>,
	inputSupplier: MotionComponentSupplier<Double>,
	motionComponent: MotionComponents,
	toleranceEpsilon: Double,
	outputConsumer: Consumer<Double> = Consumer {},
	controllerCalculation: ControllerCalculation<Double>,
	modifier: Modifier<Double> = Modifier { it },
) : Controller<Double>(
	targetSupplier,
	inputSupplier,
	motionComponent,
	toleranceEpsilon,
	outputConsumer,
	controllerCalculation,
	modifier
), EnhancedComparableNumericSupplier<Double, Conditional<Double>> {
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
		targetSupplier: Supplier<out Double>,
		inputSupplier: MotionComponentSupplier<Double>,
		motionComponent: MotionComponents,
		toleranceEpsilon: Double,
		outputConsumer: Consumer<Double> = Consumer {},
		controllerCalculation: ControllerCalculation<Double>,
		modifier: Modifier<Double> = Modifier { it },
	) : this(
		object : MotionComponentSupplier<Double> {
			override fun component(motionComponent: MotionComponents) = targetSupplier.get()
			override fun componentError(
				motionComponent: MotionComponents,
				target: Double
			) = target - targetSupplier.get()
		},
		inputSupplier,
		motionComponent,
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation,
		modifier
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
		target: Double,
		inputSupplier: MotionComponentSupplier<Double>,
		motionComponent: MotionComponents,
		toleranceEpsilon: Double,
		outputConsumer: Consumer<Double> = Consumer {},
		controllerCalculation: ControllerCalculation<Double>,
		modifier: Modifier<Double> = Modifier { it }
	) : this ({ target }, inputSupplier, motionComponent, toleranceEpsilon, outputConsumer, controllerCalculation, modifier)

	//
	// Controller
	//
	override var target: Double
		get() = targetSupplier.component(motionComponent)
		set(value) {
			targetSupplier = object : MotionComponentSupplier<Double> {
				override fun component(motionComponent: MotionComponents) = value
				override fun componentError(
					motionComponent: MotionComponents,
					target: Double
				) = target - value
			}
		}
	override fun finished(toleranceEpsilon: Double) = abs(inputSupplier.componentError(motionComponent, target)) <= toleranceEpsilon

	//
	// EnhancedNumericSupplier
	//
	override val zero = 0.0
	private var offset = zero
	override var position: Double
		get() = get() - offset
		set(value) {
			offset = current - value
		}
	override val velocity get() = previousPositions.homogenise().getVelocity()
	override val rawVelocity get() = previousPositions.last().getVelocity()
	override val acceleration get() = previousVelocities.homogenise().getVelocity()
	override val rawAcceleration get() = previousVelocities.last().getVelocity()
	override fun findErrorPosition(target: Double) = target - position
	override fun findErrorVelocity(target: Double) = target - velocity
	override fun findErrorRawVelocity(target: Double) = target - rawVelocity
	override fun findErrorAcceleration(target: Double) = target - acceleration
	override fun findErrorRawAcceleration(target: Double) = target - rawAcceleration
	override fun <N2> merge(supplier: Supplier<out N2>, merge: (Double, N2) -> Double) = EnhancedDoubleSupplier({ merge(get(), supplier.get()) }, modifier)
	override fun applyModifier(modifier: Modifier<Double>) = DoubleController(
		targetSupplier,
		inputSupplier,
		motionComponent,
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation
	) { modifier.modify(this.modifier.modify(it)) }
	override fun setModifier(modifier: Modifier<Double>) = DoubleController(
		targetSupplier,
		inputSupplier,
		motionComponent,
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation,
		modifier
	)

	//
	// Comparable
	//
	override fun conditionalBindPosition() = Conditional(this::position)
	override fun conditionalBindVelocity() = Conditional(this::velocity)
	override fun conditionalBindVelocityRaw() = Conditional(this::rawVelocity)
	override fun conditionalBindAcceleration() = Conditional(this::acceleration)
	override fun conditionalBindAccelerationRaw() = Conditional(this::rawAcceleration)
}