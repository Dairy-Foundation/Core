package dev.frozenmilk.dairy.core.util.supplier.numeric

import dev.frozenmilk.dairy.core.util.supplier.logical.Conditional

interface EnhancedComparableSupplier<T: Comparable<T>> {
	fun conditionalBindPosition(): Conditional<T>
	fun conditionalBindVelocity(): Conditional<T>
	fun conditionalBindVelocityRaw(): Conditional<T>
	fun conditionalBindAcceleration(): Conditional<T>
	fun conditionalBindAccelerationRaw(): Conditional<T>
}