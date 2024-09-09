package dev.frozenmilk.dairy.core.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import java.util.function.Function

class AllFeatureClasses(features: Set<Class<out Feature>>) : FeatureDependency<List<Feature>>(Function { collection ->
	val intersect = collection.filter { features.contains(it.javaClass) }
	if (intersect.size == features.size) return@Function intersect
	else {
		val intersectClasses = intersect.map { it.javaClass }
		throw DependencyResolutionException(
				features.filter { !intersectClasses.contains(it) }
						.map { "No feature of type ${it.simpleName}" }
		)
	}
}) {
	@SafeVarargs
	constructor(vararg features: Class<out Feature>) : this(features.toSet())
	constructor(features: Collection<Class<out Feature>>) : this(features.toSet())
}