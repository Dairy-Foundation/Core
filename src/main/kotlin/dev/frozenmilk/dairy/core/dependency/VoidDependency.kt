package dev.frozenmilk.dairy.core.dependency

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.wrapper.Wrapper

/**
 * a Java helper, do not use in Kotlin
 */
interface VoidDependency : Dependency<Unit> {
	override fun resolve(opMode: Wrapper, resolvedFeatures: List<Feature>, yielding: Boolean)
}