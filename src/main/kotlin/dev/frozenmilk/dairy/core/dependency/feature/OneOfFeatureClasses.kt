package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import java.util.function.Function

class OneOfFeatureClasses(features: Set<Class<out Feature>>) : FeatureDependency<Feature>(Function { collection ->
	val intersect = collection.filter { features.contains(it.javaClass) }
	if (intersect.size == 1) return@Function intersect.first()
	if (intersect.isNotEmpty()) {
		val intersectClasses = intersect.map { it.javaClass }
		throw DependencyResolutionException(
				intersectClasses.filter { features.contains(it) }
						.map { it to "Too many features" }
		)
	}
	throw DependencyResolutionException(
			features.map { "No feature of type ${it.simpleName}" }
	)
}) {
	@SafeVarargs
	constructor(vararg features: Class<out Feature>) : this(features.toSet())
	constructor(features: Collection<Class<out Feature>>) : this(features.toSet())
}