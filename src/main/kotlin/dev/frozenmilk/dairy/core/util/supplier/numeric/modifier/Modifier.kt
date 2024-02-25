package dev.frozenmilk.dairy.core.util.supplier.numeric.modifier

@FunctionalInterface
fun interface Modifier<T> {
	fun modify(t: T): T
}