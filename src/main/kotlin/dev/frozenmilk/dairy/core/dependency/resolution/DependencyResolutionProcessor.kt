package dev.frozenmilk.dairy.core.dependency.resolution

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.DependencyBase
import dev.frozenmilk.dairy.core.wrapper.Wrapper

internal fun resolveDependencies(wrapper: Wrapper, toResolve: MutableSet<Feature>, resolved: MutableSet<Feature>): Map<Feature, Throwable?> {
	var notLocked = true
	var yielding = false

	val exceptionMap = mutableMapOf<Feature, Throwable?>()
	while (notLocked || yielding) {
		val unresolvedSize = toResolve.size
		with(toResolve.iterator()){
			forEach { feature ->
				exceptionMap.remove(feature)

				when (val dependency = feature.dependency) {
					// needs more stuff to happen
					is DependencyBase<*> -> {
						try {
							dependency.resolveAndAccept(wrapper, resolved.toList(), yielding)
						}
						catch (e: Throwable) {
							exceptionMap[feature] = e
							null
						}?.run() // accept the result, which CAN fail, so happens outside of the try-catch
					}
					else -> {
						try {
							dependency.resolve(wrapper, resolved.toList(), yielding)
						}
						catch (e: Throwable) {
							exceptionMap[feature] = e
						}
					}
				}
				if (exceptionMap[feature] == null) {
					resolved.add(feature)
					remove()
					exceptionMap.remove(feature)
				}
			}
		}

		notLocked = toResolve.size != unresolvedSize
		yielding = (!notLocked && !yielding)
	}

	toResolve
			.forEach {
				val dependency = it.dependency
				if (dependency !is DependencyBase<*>) return@forEach
				dependency.acceptErr(exceptionMap[it]!!)
			}

	return exceptionMap
}