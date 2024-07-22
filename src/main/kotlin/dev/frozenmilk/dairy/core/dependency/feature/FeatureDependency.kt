package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.Function

open class FeatureDependency<T> (val f: Function<Collection<Feature>, T>) : Dependency<T> {
	override fun resolve(opMode: Wrapper, resolvedFeatures: List<Feature>, yielding: Boolean) = f.apply(resolvedFeatures)
}