package dev.frozenmilk.dairy.core.dependency.annotation

import dev.frozenmilk.dairy.core.dependency.DependencyBase
import java.util.function.Function

open class AnnotationDependency <T> (f: Function<List<Annotation>, T>) : DependencyBase<T>({ wrapper, _, _ ->
	f.apply(wrapper.inheritedAnnotations)
})