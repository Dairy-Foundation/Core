package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

class OneOfFeatureClasses(features: Set<Class<out Feature>>) : FeatureDependency<Feature>({ collection ->
	val classCollection = collection.associateWith { it::class.java }
	val intersect = classCollection.filterKeys { collection.contains(it) }
	if (intersect.size == 1) intersect.keys.first()
	if (intersect.isNotEmpty()) {
		throw DependencyResolutionException(
				intersect.filterValues { features.contains(it) }
						.map { (k, _) -> k to "Too many features" }
		)
	}
	throw DependencyResolutionException(
			features.filter { !intersect.containsValue(it) }
					.map { "No feature of type ${it.simpleName}" }
	)
}) {
	constructor(vararg features: Class<out Feature>) : this(features.toSet())
	constructor(features: Collection<Class<out Feature>>) : this(features.toSet())
}