package dev.frozenmilk.dairy.core.util

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.cell.LazyCell
import java.util.function.Supplier

/**
 * A [LazyCell] that is initialised on the init of an OpMode
 */
class OpModeLazyCell<T>(supplier: Supplier<T>) : LazyCell<T>(supplier), Feature {
	override val dependency = Yielding

	override fun get(): T {
		if(!initialised() && !FeatureRegistrar.opModeActive) throw IllegalStateException("Attempted to evaluate contents of OpModeLazyCell while no opmode active")
		return super.get()
	}

	init {
		FeatureRegistrar.registerFeature(this)
	}

	override fun preUserInitHook(opMode: Wrapper) { get() }

	override fun postUserStopHook(opMode: Wrapper) {
		FeatureRegistrar.deregisterFeature(this)
	}
}

@JvmName("CellUtils")
fun <T> Supplier<T>.intoOpModeLazyCell() = OpModeLazyCell(this)