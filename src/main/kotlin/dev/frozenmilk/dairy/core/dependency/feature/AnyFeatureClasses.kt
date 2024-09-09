package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

class AnyFeatureClasses(features: Set<Class<out Feature>>) : FeatureDependency<List<Feature>>({ collection ->
	val classCollection = collection.map { it to it.javaClass }
	val intersect = classCollection
		.filter { (_, clazz) -> features.any { it.isAssignableFrom(clazz) } }
		.map { it.first }
	intersect.ifEmpty {
		throw DependencyResolutionException(
			features.map { "No annotation of type ${it.simpleName}" }
		)
	}
}) {
	@SafeVarargs
	constructor(vararg features: Class<out Feature>) : this(features.toSet())
	constructor(features: Collection<Class<out Feature>>) : this(features.toSet())
}