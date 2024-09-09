package dev.frozenmilk.dairy.core.util.supplier.logical

interface IConditional<T : Comparable<T>> {
	/**
	 * @return self, for chaining
	 */
	fun lessThan(value: T): IConditional<T>

	/**
	 * @return self, for chaining
	 */
	fun lessThanEqualTo(value: T): IConditional<T>

	/**
	 * @return self, for chaining
	 */
	fun greaterThan(value: T): IConditional<T>

	/**
	 * @return self, for chaining
	 */
	fun greaterThanEqualTo(value: T): IConditional<T>
	fun bind(): IEnhancedBooleanSupplier<*>
}