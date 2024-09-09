package dev.frozenmilk.dairy.core.util

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.cell.LazyCell
import java.util.function.Supplier

/**
 * A [LazyCell] that is eagerly initialised on the init of an OpMode
 *
 * This is [deregister]ed at the end of the OpMode, see [OpModeFreshLazyCell] if you don't want this behaviour.
 *
 * Or, you can manually re[register] this.
 */
class OpModeLazyCell<T>(supplier: Supplier<T>) : LazyCell<T>(supplier), Feature {
	override var dependency: Dependency<*> = Yielding

	override fun get(): T {
		if(!initialised && !FeatureRegistrar.opModeRunning) throw IllegalStateException("Attempted to evaluate contents of OpModeLazyCell while no opmode active")
		return super.get()
	}

	init {
		FeatureRegistrar.registerFeature(this)
	}

	override fun preUserInitHook(opMode: Wrapper) { get() }

	override fun cleanup(opMode: Wrapper) { deregister() }
}