package dev.frozenmilk.dairy.core.util

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.cell.LazyCell
import java.util.function.Supplier

/**
 * A [LazyCell] that is invalidated and initialised on the init of an OpMode
 *
 * unlike an [OpModeLazyCell], this is suitable for use in a persistent context,
 * where this shouldn't automatically deregister
 *
 * @see OpModeLazyCell for use in an op mode
 */
class OpModeFreshLazyCell<T>(supplier: Supplier<T>) : LazyCell<T>(supplier), Feature {
    override var dependency: Dependency<*> = Yielding

    override fun get(): T {
        if(!initialised() && !FeatureRegistrar.opModeActive) throw IllegalStateException("Attempted to evaluate contents of OpModeFreshLazyCell while no opmode active")
        return super.get()
    }

    init {
        FeatureRegistrar.registerFeature(this)
    }

    override fun preUserInitHook(opMode: Wrapper) {
        invalidate()
        get()
    }
}