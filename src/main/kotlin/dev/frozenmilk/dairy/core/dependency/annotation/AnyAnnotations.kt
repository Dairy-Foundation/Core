package dev.frozenmilk.dairy.core.dependency.annotation

import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

class AnyAnnotations(annotations: Set<Class<out Annotation>>) : AnnotationDependency<Set<Annotation>>({ collection ->
	val classCollection = collection.associateWith { it::class.java }
	val intersect = classCollection.filterValues { clazz -> annotations.any { it.isAssignableFrom(clazz) } }
	intersect.keys.ifEmpty {
		throw DependencyResolutionException(
				annotations.filter { !intersect.containsValue(it) }
						.map { "No annotation of type ${it.simpleName}" }
		)
	}
}) {
	constructor(vararg annotations: Class<out Annotation>) : this(annotations.toSet())
	constructor(annotations: Collection<Class<out Annotation>>) : this(annotations.toSet())
}