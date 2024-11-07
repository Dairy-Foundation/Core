package dev.frozenmilk.dairy.core

import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.sinister.loading.Preload

/**
 * Objects which implement this can run actions against a wide range of hooks into all OpModes
 *
 * Instances can be registered against the EventRegistrar using .registerListener(this), and should do so on instantiation
 *
 * Instances can be deregistered against the EventRegistrar using .deregisterListener(this), should they wish to do so of their own violation
 *
 * WARNING: all classes that implement [Feature] are [Preload]ed by [dev.frozenmilk.sinister.Sinister] via the [FeatureSinisterFilter], this should most likely not cause issues
 */
@Preload
interface Feature {
	/**
	 * the dependencies required by this Feature for it to be successfully enabled
	 */
	var dependency: Dependency<*>

	fun preUserInitHook(opMode: Wrapper) {}

	fun postUserInitHook(opMode: Wrapper) {}

	fun preUserInitLoopHook(opMode: Wrapper) {}

	fun postUserInitLoopHook(opMode: Wrapper) {}

	fun preUserStartHook(opMode: Wrapper) {}

	fun postUserStartHook(opMode: Wrapper) {}

	fun preUserLoopHook(opMode: Wrapper) {}

	fun postUserLoopHook(opMode: Wrapper) {}

	fun preUserStopHook(opMode: Wrapper) {}

	/**
	 * will not get run if [opMode] crashed
	 *
	 * @see cleanup
	 */
	fun postUserStopHook(opMode: Wrapper) {}

	/**
	 * runs after [opMode] stops, regardless of it crashed or not
	 *
	 * at this point, [FeatureRegistrar.opModeRunning] will be false
	 *
	 * @see postUserStopHook
	 */
	fun cleanup(opMode: Wrapper) {}

	fun register(): Feature { return also { FeatureRegistrar.registerFeature(this) } }

	fun deregister(): Feature { return also { FeatureRegistrar.deregisterFeature(this) } }

	val active
		@Suppress("INAPPLICABLE_JVM_NAME")
		@JvmName("isActive")
		get() = FeatureRegistrar.isFeatureActive(this)
}
