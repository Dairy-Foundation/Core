package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

class SingleFeature<T: Feature>(feature: T) : FeatureDependency<T> ({
	if (it.contains(feature)) feature
	else throw DependencyResolutionException(feature to "feature not attached")
})