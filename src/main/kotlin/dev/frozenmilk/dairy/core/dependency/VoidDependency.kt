package dev.frozenmilk.dairy.core.dependency

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.wrapper.Wrapper

/**
 * a Java helper, do not use in Kotlin
 */
@FunctionalInterface
@JvmDefaultWithoutCompatibility
fun interface VoidDependency : Dependency<Unit> {
	override fun resolve(opMode: Wrapper, resolvedFeatures: List<Feature>, yielding: Boolean) = voidResolve(opMode, resolvedFeatures, yielding)
	fun voidResolve(opMode: Wrapper, resolvedFeatures: List<Feature>, yielding: Boolean)
}