package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.controller.Controller
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import dev.frozenmilk.dairy.core.util.supplier.logical.Conditional
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedComparableNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.util.supplier.numeric.unit.EnhancedUnitSupplier
import dev.frozenmilk.util.modifier.Modifier
import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit
import dev.frozenmilk.util.units.getVelocity
import dev.frozenmilk.util.units.homogenise
import java.util.function.Consumer
import java.util.function.Supplier

/**
 * [deregister]s at the end of the OpMode,
 * it is recommended to either re-[register] if you wish to re-use this,
 * or regenerate for each OpMode (which must be done if this is built from regenerated resourced, like motors or encoders)
 */
class UnitController<U: Unit<U>, RU: ReifiedUnit<U, RU>>
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
	targetSupplier: MotionComponentSupplier<out RU>,
	inputSupplier: MotionComponentSupplier<RU>,
	motionComponent: MotionComponents,
	toleranceEpsilon: RU,
	outputConsumer: Consumer<RU> = Consumer {},
	controllerCalculation: ControllerCalculation<RU>,
	modifier: Modifier<RU> = Modifier { it }
) : Controller<RU>(
	targetSupplier,
	inputSupplier,
	motionComponent,
	toleranceEpsilon,
	outputConsumer,
	controllerCalculation,
	modifier
), EnhancedComparableNumericSupplier<RU, Conditional<RU>> {
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
		targetSupplier: Supplier<out RU>,
		inputSupplier: MotionComponentSupplier<RU>,
		motionComponent: MotionComponents,
		toleranceEpsilon: RU,
		outputConsumer: Consumer<RU> = Consumer {},
		controllerCalculation: ControllerCalculation<RU>,
		modifier: Modifier<RU> = Modifier { it }
	) : this(
		object : MotionComponentSupplier<RU> {
			override fun component(motionComponent: MotionComponents) = targetSupplier.get()
			override fun componentError(
				motionComponent: MotionComponents,
				target: RU
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
		target: RU,
		inputSupplier: MotionComponentSupplier<RU>,
		motionComponent: MotionComponents,
		toleranceEpsilon: RU,
		outputConsumer: Consumer<RU> = Consumer {},
		controllerCalculation: ControllerCalculation<RU>,
		modifier: Modifier<RU> = Modifier { it }
	) : this (Supplier { target }, inputSupplier, motionComponent, toleranceEpsilon, outputConsumer, controllerCalculation, modifier)

	//
	// Controller
	//
	override var target: RU
		get() = targetSupplier.component(motionComponent)
		set(value) {
			targetSupplier = object : MotionComponentSupplier<RU> {
				override fun component(motionComponent: MotionComponents) = value
				override fun componentError(
					motionComponent: MotionComponents,
					target: RU
				) = target - value
			}
		}
	override fun finished(toleranceEpsilon: RU) = inputSupplier.componentError(motionComponent, target).abs() <= toleranceEpsilon

	//
	// EnhancedNumericSupplier
	//
	override val zero = toleranceEpsilon.run { this - this }
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
	override fun findErrorPosition(target: RU) = position.findError(target)
	override fun findErrorVelocity(target: RU) = velocity.findError(target)
	override fun findErrorRawVelocity(target: RU) = rawVelocity.findError(target)
	override fun findErrorAcceleration(target: RU) = acceleration.findError(target)
	override fun findErrorRawAcceleration(target: RU) = rawAcceleration.findError(target)
	override fun <N2> merge(supplier: Supplier<out N2>, merge: (RU, N2) -> RU) = EnhancedUnitSupplier({ merge(get(), supplier.get()) }, modifier)
	override fun applyModifier(modifier: Modifier<RU>) = UnitController(
		targetSupplier,
		inputSupplier,
		motionComponent,
		toleranceEpsilon,
		outputConsumer,
		controllerCalculation
	) { modifier.modify(this.modifier.modify(it)) }
	override fun setModifier(modifier: Modifier<RU>) = UnitController(
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