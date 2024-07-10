package dev.frozenmilk.dairy.core.dependency.annotation

import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

class SingleAnnotations<T: Annotation>(annotation: Class<T>) : AnnotationDependency<List<T>>({
	it.filterIsInstance(annotation).ifEmpty {
		throw DependencyResolutionException("no instances of ${annotation.simpleName}")
	}
})