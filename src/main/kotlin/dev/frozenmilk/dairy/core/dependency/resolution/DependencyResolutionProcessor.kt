package dev.frozenmilk.dairy.core.dependency.resolution

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.resolveAndAccept
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

				try {
					feature.dependency.resolveAndAccept(wrapper, resolved.toList(), yielding)
				}
				catch (e: Dependency.CallbackErr) {
					throw e.cause
				}
				catch (e: Throwable) {
					exceptionMap[feature] = e
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
				it.dependency.acceptErr(exceptionMap[it]!!)
			}

	return exceptionMap
}