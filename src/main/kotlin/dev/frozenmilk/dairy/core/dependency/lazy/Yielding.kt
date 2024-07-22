package dev.frozenmilk.dairy.core.dependency.lazy

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import dev.frozenmilk.dairy.core.wrapper.Wrapper

object Yielding : Dependency<Unit> {
	override fun resolve(opMode: Wrapper, resolvedFeatures: List<Feature>, yielding: Boolean) = if (!yielding) throw DependencyResolutionException("not yet yielding") else Unit
}