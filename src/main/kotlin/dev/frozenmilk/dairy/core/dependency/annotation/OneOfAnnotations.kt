package dev.frozenmilk.dairy.core.dependency.annotation

import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

class OneOfAnnotations(annotations: Set<Class<out Annotation>>) : AnnotationDependency<Annotation>({ collection ->
	val classCollection = collection.associateWith { it::class.java }
	val intersect = classCollection.filterValues { clazz -> annotations.any { it.isAssignableFrom(clazz) } }
	if (intersect.size == 1) intersect.keys.first()
	if (intersect.isNotEmpty()) {
		throw DependencyResolutionException(
				intersect.filterValues { annotations.contains(it) }
						.map {
							(k, _) -> k to "Too many annotations"
						}
		)
	}
	throw DependencyResolutionException(
			annotations.filter { !intersect.containsValue(it) }
					.map { "No annotation of type ${it.simpleName}" }
	)
}) {
	constructor(vararg annotations: Class<out Annotation>) : this(annotations.toSet())
	constructor(annotations: Collection<Class<out Annotation>>) : this(annotations.toSet())
}