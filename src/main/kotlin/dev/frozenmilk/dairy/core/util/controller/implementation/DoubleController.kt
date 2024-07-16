package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.controller.calculation.ControllerCalculation
import dev.frozenmilk.dairy.core.util.controller.Controller
import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedDoubleSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.IEnhancedNumericSupplier
import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponents
import java.util.function.Consumer
import java.util.function.Supplier
import kotlin.math.abs

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
class DoubleController(
	targetSupplier: Supplier<out Double>,
	inputSupplier: IEnhancedNumericSupplier<Double>,
	motionComponent: MotionComponents,
	toleranceEpsilon: Double,
	outputConsumer: Consumer<Double>,
	controllerCalculation: ControllerCalculation<Double>
) : Controller<Double>(
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
		target: Double,
		inputSupplier: IEnhancedNumericSupplier<Double>,
		motionComponent: MotionComponents,
		toleranceEpsilon: Double,
		outputConsumer: Consumer<Double> = Consumer {},
		controllerCalculation: ControllerCalculation<Double>,
	) : this (Supplier { target }, inputSupplier, motionComponent, toleranceEpsilon, outputConsumer, controllerCalculation)
	override val zero = 0.0
	override val supplier by lazy { EnhancedDoubleSupplier(this::output) }
	override fun finished(toleranceEpsilon: Double) = abs(error()) <= toleranceEpsilon
}