package dev.frozenmilk.dairy.core.dependency

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.Consumer

open class DependencyBase <T> (dependency: Dependency<T>) : Dependency<T> by dependency {
	private val onResolveConsumers = mutableListOf<Consumer<T>>()
	private val onFailConsumers = mutableListOf<Consumer<Throwable>>()
	fun onResolve(receiver: Consumer<T>): DependencyBase<T> {
		onResolveConsumers.add(receiver)
		return this
	}
	fun onFail(receiver: Consumer<Throwable>): DependencyBase<T> {
		onFailConsumers.add(receiver)
		return this
	}
	open fun accept(p0: T) {
		onResolveConsumers.forEach { it.accept(p0) }
	}
	open fun acceptErr(p0: Throwable) {
		onFailConsumers.forEach { it.accept(p0) }
	}
	@Throws(DependencyResolutionException::class)
	fun resolveAndAccept(opMode: Wrapper, resolvedFeatures: List<Feature>, yielding: Boolean) : Runnable {
		val res = resolve(opMode, resolvedFeatures, yielding)
		return Runnable { accept(res) }
	}
}