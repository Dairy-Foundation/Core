package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

class AnyFeatures(features: Set<Feature>) : FeatureDependency<Set<Feature>>({ collection ->
	val intersect = collection.intersect(features)
	intersect.ifEmpty {
		throw DependencyResolutionException(
				features.filter { !intersect.contains(it) }
						.map { "$it not attached" }
		)
	}
}) {
	constructor(vararg features: Feature) : this(features.toSet())
	constructor(features: Collection<Feature>) : this(features.toSet())
}