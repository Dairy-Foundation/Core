package dev.frozenmilk.dairy.core.util.supplier.numeric

import java.util.EnumMap

@FunctionalInterface
fun interface MotionComponentSupplier<T> {
	operator fun get(motionComponent: MotionComponents): T
	open fun reset() {}
}

class CachedMotionComponentSupplier<T>(val motionComponentSupplier: MotionComponentSupplier<T>) : MotionComponentSupplier<T> {
	private val map = EnumMap<MotionComponents, T>(MotionComponents::class.java)
	override fun get(motionComponent: MotionComponents): T = map.computeIfAbsent(motionComponent, motionComponentSupplier::get)
	fun invalidate() {
		map.clear()
	}
	override fun reset() {
		super.reset()
		invalidate()
	}
}