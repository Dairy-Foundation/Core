package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

class AnyFeatureClasses(features: Set<Class<out Feature>>) : FeatureDependency<Set<Feature>>({ collection ->
	val classCollection = collection.associateWith { it::class.java }
	val intersect = classCollection.filterKeys { collection.contains(it) }
	intersect.keys.ifEmpty {
		throw DependencyResolutionException(
				features.filter { !intersect.containsValue(it) }
						.map { "No feature of type ${it.simpleName}" }
		)
	}
}) {
	constructor(vararg features: Class<out Feature>) : this(features.toSet())
	constructor(features: Collection<Class<out Feature>>) : this(features.toSet())
}