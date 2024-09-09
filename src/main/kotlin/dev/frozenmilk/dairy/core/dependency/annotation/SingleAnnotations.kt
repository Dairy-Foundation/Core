package dev.frozenmilk.dairy.core.dependency.annotation

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.sinister.getAllAnnotationsByType

class SingleAnnotations<T: Annotation>(private val annotation: Class<T>) : Dependency<List<T>>{
	override fun resolve(
		opMode: Wrapper,
		resolvedFeatures: List<Feature>,
		yielding: Boolean
	): List<T> {
		return opMode.opMode.javaClass.getAllAnnotationsByType(annotation).ifEmpty {
			throw DependencyResolutionException("no instances of ${annotation.simpleName}")
		}
	}
}