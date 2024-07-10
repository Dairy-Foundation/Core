package dev.frozenmilk.dairy.core.dependency.annotation

import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

class SingleAnnotation<T: Annotation>(annotation: Class<T>) : AnnotationDependency<T>({
	it.filterIsInstance(annotation).ifEmpty {
		throw DependencyResolutionException("no instances of ${annotation.simpleName}")
	}.first()
})