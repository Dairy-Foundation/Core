package dev.frozenmilk.dairy.core.util.supplier.numeric

import dev.frozenmilk.dairy.core.util.supplier.logical.IConditional

interface EnhancedComparableSupplier<T: Comparable<T>, CONDITIONAL: IConditional<T>> {
	fun conditionalBindPosition(): CONDITIONAL
	fun conditionalBindVelocity(): CONDITIONAL
	fun conditionalBindVelocityRaw(): CONDITIONAL
	fun conditionalBindAcceleration(): CONDITIONAL
	fun conditionalBindAccelerationRaw(): CONDITIONAL
}

interface EnhancedComparableNumericSupplier<T: Comparable<T>, CONDITIONAL: IConditional<T>> : EnhancedComparableSupplier<T, CONDITIONAL>, IEnhancedNumericSupplier<T>