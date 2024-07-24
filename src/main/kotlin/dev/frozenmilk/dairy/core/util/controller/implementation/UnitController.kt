package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.controller.Controller
import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import dev.frozenmilk.dairy.core.util.supplier.numeric.unit.EnhancedUnitSupplier
import dev.frozenmilk.util.units.ReifiedUnit
import dev.frozenmilk.util.units.Unit
import java.util.function.Consumer
import java.util.function.Supplier

class UnitController<U: Unit<U>, RU: ReifiedUnit<U, RU>>
/**
 * @param targetSupplier supplier for the target position
 * @param inputSupplier supplier for the system state
 * @param motionComponent motionComponent that this controller will act on
 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
 * @param outputConsumer method to update the output consumer of the system
 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [output]
 */
@JvmOverloads
constructor(
	targetSupplier: Supplier<out RU>,
	inputSupplier: IEnhancedNumericSupplier<RU>,
	motionComponent: MotionComponents,
	toleranceEpsilon: RU,
	outputConsumer: Consumer<RU> = Consumer {},
	controllerCalculation: ControllerCalculation<RU>
) : Controller<RU>(
	targetSupplier,
	inputSupplier,
	motionComponent,
	toleranceEpsilon,
	outputConsumer,
	controllerCalculation
) {
	/**
	 * @param targetSupplier supplier for the target position
	 * @param inputSupplier supplier for the system state
	 * @param motionComponent motionComponent that this controller will act on
	 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [output]
	 */
	constructor(
		targetSupplier: Supplier<out RU>,
		inputSupplier: IEnhancedNumericSupplier<RU>,
		motionComponent: MotionComponents,
		toleranceEpsilon: RU,
		outputConsumer: Consumer<Double>,
		controllerCalculation: ControllerCalculation<RU>
	) : this(targetSupplier, inputSupplier, motionComponent, toleranceEpsilon, Consumer<RU> { outputConsumer.accept(it.intoCommon().value) }, controllerCalculation)
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
		target: RU,
		inputSupplier: IEnhancedNumericSupplier<RU>,
		motionComponent: MotionComponents,
		toleranceEpsilon: RU,
		outputConsumer: Consumer<RU> = Consumer {},
		controllerCalculation: ControllerCalculation<RU>,
	) : this (Supplier { target }, inputSupplier, motionComponent, toleranceEpsilon, outputConsumer, controllerCalculation)

	/**
	 * @param target target position
	 * @param inputSupplier supplier for the system state
	 * @param motionComponent motionComponent that this controller will act on
	 * @param toleranceEpsilon used for [finished], tolerance for determining if the system is finished
	 * @param outputConsumer method to update the output consumer of the system
	 * @param controllerCalculation [ControllerCalculation] used to transform the input of this system into [output]
	 */
	constructor(
		target: RU,
		inputSupplier: IEnhancedNumericSupplier<RU>,
		motionComponent: MotionComponents,
		toleranceEpsilon: RU,
		outputConsumer: Consumer<Double>,
		controllerCalculation: ControllerCalculation<RU>,
	) : this (Supplier { target }, inputSupplier, motionComponent, toleranceEpsilon, Consumer<RU> { outputConsumer.accept(it.intoCommon().value) }, controllerCalculation)

	override val zero = toleranceEpsilon.apply { this - this }
	override val supplier by lazy { EnhancedUnitSupplier(this::output) }
	override fun finished(toleranceEpsilon: RU) = error().abs() <= toleranceEpsilon
}