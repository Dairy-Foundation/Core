package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import java.util.function.Function

class AllFeatures(features: Set<Feature>) : FeatureDependency<Set<Feature>>(Function { collection ->
	val intersect = collection.intersect(features)
	if (intersect.size == features.size) return@Function features
	throw DependencyResolutionException(
		features.filter { !intersect.contains(it) }
			.map { "$it not attached" }
	)
}) {
	constructor(vararg features: Feature) : this(features.toSet())
	constructor(features: Collection<Feature>) : this(features.toSet())
}