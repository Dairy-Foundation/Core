package dev.frozenmilk.dairy.core.dependency

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.meta.And
import dev.frozenmilk.dairy.core.dependency.meta.Or
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import dev.frozenmilk.dairy.core.wrapper.Wrapper

@FunctionalInterface
@JvmDefaultWithCompatibility
fun interface Dependency <T> {
	/**
	 * is called to determine if a dependency is resolved or not for [opMode], [resolvedFeatures] and [yielding]
	 *
	 * @throws DependencyResolutionException to indicate failure
	 * @return to indicate success
	 */
	@Throws(DependencyResolutionException::class)
	fun resolve(opMode: Wrapper, resolvedFeatures: List<Feature>, yielding: Boolean) : T
	fun upgrade() = DependencyBase(this)

	// operators
	infix fun <R> or (rhs: Dependency<R>) = Or(this, rhs)
	infix fun <R> and (rhs: Dependency<R>) = And(this, rhs)
}

