package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.DependencyBase
import java.util.function.Function

open class FeatureDependency<T> (f: Function<Collection<Feature>, T>) : DependencyBase<T>({ _, features, _ ->
	f.apply(features)
})