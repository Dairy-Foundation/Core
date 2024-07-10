package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

class SingleFeatureClass<T: Feature>(feature: Class<T>) : FeatureDependency<List<T>>({
	it.filterIsInstance(feature).ifEmpty {
		throw DependencyResolutionException("no instances of ${feature.simpleName} attached")
	}
})