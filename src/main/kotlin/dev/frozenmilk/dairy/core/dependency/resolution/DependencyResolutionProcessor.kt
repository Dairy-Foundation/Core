package dev.frozenmilk.dairy.core.dependency.resolution

import com.qualcomm.robotcore.util.RobotLog
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
					RobotLog.ee("DependencyResolution", e.cause, "error was thrown while running onResolve callbacks for dependencies, logged but not thrown")
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
				val dependency = it.dependency
				try {
					dependency.acceptErr(exceptionMap[it]!!)
				}
				catch (e: Throwable) {
					RobotLog.ee("DependencyResolution", e, "error was thrown while running onFail callbacks for dependencies, logged but not thrown")
				}
			}

	return exceptionMap
}