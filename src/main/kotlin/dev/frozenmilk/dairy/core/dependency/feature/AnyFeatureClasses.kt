package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

class AnyFeatureClasses(features: Set<Class<out Feature>>) : FeatureDependency<List<Feature>>({ collection ->
	collection.filter { features.contains(it.javaClass) }.ifEmpty {
		throw DependencyResolutionException(
			features.map { "No feature of type ${it.simpleName}" }
		)
	}
}) {
	constructor(vararg features: Class<out Feature>) : this(features.toSet())
	constructor(features: Collection<Class<out Feature>>) : this(features.toSet())
}