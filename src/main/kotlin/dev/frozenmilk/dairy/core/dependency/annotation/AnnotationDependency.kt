package dev.frozenmilk.dairy.core.dependency.annotation

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.Function

open class AnnotationDependency <T> (val f: Function<List<Annotation>, T>) : Dependency<T> {
	override fun resolve(opMode: Wrapper, resolvedFeatures: List<Feature>, yielding: Boolean) = f.apply(opMode.inheritedAnnotations)
}