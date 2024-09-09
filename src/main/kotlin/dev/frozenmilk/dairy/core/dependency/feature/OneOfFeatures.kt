package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import java.util.function.Function

class OneOfFeatures(features: Set<Feature>) : FeatureDependency<Feature>(Function { collection ->
	val intersect = collection.intersect(features)
	if (intersect.size == 1) return@Function intersect.first()
	if (intersect.isNotEmpty()) {
		throw DependencyResolutionException(
				intersect.filter { features.contains(it) }
						.map { it to "Too many features" }
		)
	}
	throw DependencyResolutionException(
			features.filter { !intersect.contains(it) }
					.map { "$it not attached" }
	)
}) {
	constructor(vararg features: Feature) : this(features.toSet())
	constructor(features: Collection<Feature>) : this(features.toSet())
}