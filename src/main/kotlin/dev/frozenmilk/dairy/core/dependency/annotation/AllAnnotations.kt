package dev.frozenmilk.dairy.core.dependency.annotation

import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import java.util.function.Function

class AllAnnotations(annotations: Set<Class<out Annotation>>) : AnnotationDependency<List<Annotation>>(Function { collection ->
		val classCollection = collection.map { it to it.javaClass }
		val intersect = classCollection
			.filter { (_, clazz) -> annotations.any { it.isAssignableFrom(clazz) } }
			.map { it.first }
		if (intersect.size == annotations.size) return@Function intersect
		val unFound = classCollection.map { it.second }
		throw DependencyResolutionException(
				annotations
					.filter { !unFound.contains(it) }
					.map { "No annotation of type ${it.simpleName}" }
		)
}) {
	@SafeVarargs
	constructor(vararg annotations: Class<out Annotation>) : this(annotations.toSet())
	constructor(annotations: Collection<Class<out Annotation>>) : this(annotations.toSet())
}