package dev.frozenmilk.dairy.core.dependency

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.wrapper.Wrapper

open class DependencyBase <T> (val dependency: Dependency<T>) : Dependency<T> {
	override fun resolve(opMode: Wrapper, resolvedFeatures: List<Feature>, yielding: Boolean) = dependency.resolve(opMode, resolvedFeatures, yielding)
	override fun accept(p0: T) = dependency.accept(p0)
	override fun acceptErr(p0: Throwable) = dependency.acceptErr(p0)
}