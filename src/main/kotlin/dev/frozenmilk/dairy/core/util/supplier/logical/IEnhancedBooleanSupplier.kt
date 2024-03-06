package dev.frozenmilk.dairy.core.util.supplier.logical

import dev.frozenmilk.dairy.core.Feature
import java.util.function.Supplier

@Suppress("INAPPLICABLE_JVM_NAME")
interface IEnhancedBooleanSupplier : Feature {
	@get:JvmName("toggleTrue")
	val toggleTrue: Boolean

	@get:JvmName("toggleFalse")
	val toggleFalse: Boolean

	/**
	 * returns the current boolean state of this
	 */
	@get:JvmName("state")
	val state: Boolean

	/**
	 * a rising edge detector for this
	 */
	@get:JvmName("onTrue")
	val onTrue: Boolean

	/**
	 * a falling edge detector for this
	 */
	@get:JvmName("onFalse")
	val onFalse: Boolean

	/**
	 * if this automatically updates, by calling [invalidate] and [state]
	 */
	var autoUpdates: Boolean

	/**
	 * causes the next call to [get] to update this supplier
	 */
	fun invalidate()

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to both the rising and falling edges
	 */
	fun debounce(debounce: Double): IEnhancedBooleanSupplier

	/**
	 * non-mutating
	 *
	 * @param rising is applied to the rising edge
	 * @param falling is applied to the falling edge
	 */
	fun debounce(rising: Double, falling: Double): IEnhancedBooleanSupplier

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to the rising edge
	 */
	fun debounceRisingEdge(debounce: Double): IEnhancedBooleanSupplier

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to the falling edge
	 */
	fun debounceFallingEdge(debounce: Double): IEnhancedBooleanSupplier

	/**
	 * non-mutating
	 *
	 * @return a new IEnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun and(booleanSupplier: Supplier<Boolean>): IEnhancedBooleanSupplier

	/**
	 * non-mutating
	 *
	 * @return a new IEnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun and(booleanSupplier: IEnhancedBooleanSupplier): IEnhancedBooleanSupplier

	/**
	 * non-mutating
	 *
	 * @return a new IEnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun or(booleanSupplier: Supplier<Boolean>): IEnhancedBooleanSupplier

	/**
	 * non-mutating
	 *
	 * @return a new IEnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun or(booleanSupplier: IEnhancedBooleanSupplier): IEnhancedBooleanSupplier

	/**
	 * non-mutating
	 *
	 * @return a new IEnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun xor(booleanSupplier: Supplier<Boolean>): IEnhancedBooleanSupplier

	/**
	 * non-mutating
	 *
	 * @return a new IEnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun xor(booleanSupplier: IEnhancedBooleanSupplier): IEnhancedBooleanSupplier

	/**
	 * non-mutating
	 *
	 * @return a new IEnhancedBooleanSupplier that has the inverse of this, and keeps the debounce information
	 */
	operator fun not(): IEnhancedBooleanSupplier
}