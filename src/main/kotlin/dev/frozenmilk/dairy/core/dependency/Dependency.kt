package dev.frozenmilk.dairy.core.dependency

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency.CallbackErr
import dev.frozenmilk.dairy.core.dependency.meta.And
import dev.frozenmilk.dairy.core.dependency.meta.Or
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.Consumer

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

	/**
	 * calls all [onResolve] consumers
	 */
	fun accept(p0: T) {}
	fun onResolve(receiver: Consumer<T>): Dependency<T> = object : DependencyBase<T>(this) {
		override fun accept(p0: T) {
			super.accept(p0)
			receiver.accept(p0)
		}
	}

	/**
	 * calls all [onFail] consumers
	 */
	fun acceptErr(p0: Throwable) {}
	fun onFail(receiver: Consumer<Throwable>): Dependency<T> = object : DependencyBase<T>(this) {
		override fun acceptErr(p0: Throwable) {
			super.acceptErr(p0)
			receiver.accept(p0)
		}
	}

	// operators
	infix fun <R> or (rhs: Dependency<R>) = Or(this, rhs)
	infix fun <R> and (rhs: Dependency<R>) = And(this, rhs)

	/**
	 * WARNING, NOT TO BE CONSTRUCTED OUTSIDE OF THE SINGLE USAGE IN THIS FILE
	 *
	 * marker wrapper
 	 */
	class CallbackErr internal constructor(override val cause: Throwable) : Throwable()
}

/**
 * NOTE: this is an extension function in order to avoid stupid jvm interface bullshit that would
 * prevent it from calling the correct [Dependency.accept], as it would call the super one
 */
@Throws(DependencyResolutionException::class)
fun <T> Dependency<T>.resolveAndAccept(opMode: Wrapper, resolvedFeatures: List<Feature>, yielding: Boolean) {
	val res = resolve(opMode, resolvedFeatures, yielding)
	return try {
		this.accept(res)
	}
	catch (e: Throwable) {
		throw CallbackErr(e) // wrap to distinguish
	}
}

