package dev.frozenmilk.dairy.core.util.supplier.numeric

interface MotionComponentSupplier<N> {
	fun component(motionComponent: MotionComponents): N
	fun componentError(motionComponent: MotionComponents, target: N): N
}