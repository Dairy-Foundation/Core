package dev.frozenmilk.dairy.core.util

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependencyresolution.dependencies.Dependency
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.cell.LazyCell
import java.util.function.Supplier

/**
 * A [LazyCell] that is invalidated and initialised on the init of an OpMode
 */
class OpModeFreshLazyCell<T>(supplier: Supplier<T>) : LazyCell<T>(supplier), Feature {
    override val dependencies: Set<Dependency<*, *>> = DependencySet(this)
        .yields()

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

@JvmName("CellUtils")
fun <T> Supplier<T>.intoOpModeFreshLazyCell() = OpModeFreshLazyCell(this)