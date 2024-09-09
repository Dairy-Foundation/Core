package dev.frozenmilk.dairy.core.dependency.annotation

import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

class AnyAnnotations(annotations: Set<Class<out Annotation>>) : AnnotationDependency<List<Annotation>>({ collection ->
	val classCollection = collection.map { it to it.javaClass }
	val intersect = classCollection
		.filter { (_, clazz) -> annotations.any { it.isAssignableFrom(clazz) } }
		.map { it.first }
	intersect.ifEmpty {
		throw DependencyResolutionException(
				annotations.map { "No annotation of type ${it.simpleName}" }
		)
	}
}) {
	@SafeVarargs
	constructor(vararg annotations: Class<out Annotation>) : this(annotations.toSet())
	constructor(annotations: Collection<Class<out Annotation>>) : this(annotations.toSet())
}