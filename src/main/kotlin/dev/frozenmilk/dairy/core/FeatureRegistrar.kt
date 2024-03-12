package dev.frozenmilk.dairy.core

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.dairy.core.dependencyresolution.DependencyResolutionFailureException
import dev.frozenmilk.dairy.core.dependencyresolution.FeatureDependencyResolutionFailureException
import dev.frozenmilk.dairy.core.dependencyresolution.plus
import dev.frozenmilk.dairy.core.dependencyresolution.resolveDependencies
import dev.frozenmilk.dairy.core.wrapper.LinearOpModeWrapper
import dev.frozenmilk.dairy.core.wrapper.OpModeWrapper
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.util.cell.LateInitCell
import dev.frozenmilk.util.cell.LazyCell
import dev.frozenmilk.util.cell.MirroredCell
import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop
import org.firstinspires.ftc.robotcore.internal.opmode.RegisteredOpModes
import java.lang.ref.WeakReference
import java.util.ArrayDeque
import java.util.Queue

object FeatureRegistrar : OpModeManagerNotifier.Notifications {
	private const val TAG = "DairyCore"
	/**
	 * features that are registered to potentially become active
	 */
	private var registeredFeatures: MutableSet<WeakReference<Feature>> = mutableSetOf()

	/**
	 * intermediary collection of features that need to be checked to be added to the active pool
	 */
	private val registrationQueue: Queue<Pair<WeakReference<Feature>, Boolean>> = ArrayDeque()

	/**
	 * features that have been activated via [resolveDependencies]
	 */
	private val activeFeatures: MutableSet<Feature> = mutableSetOf()

	/**
	 * the feature flag annotations of the active OpMode
	 */
	private val activeFlags: MutableSet<Annotation> = mutableSetOf()

	/**
	 * if there is currently an [OpMode] active on the robot, should be true almost all of the time
	 */
	@JvmStatic
	var opModeActive: Boolean = false
		private set

	@JvmStatic
	val opModeState: Wrapper.OpModeState
		get() = activeOpModeWrapper.state

	/**
	 * the mirror cell used to manage this
	 */
	private var activeOpModeMirroredCell = LazyCell {
		MirroredCell<OpMode>(opModeManager, "activeOpMode")
	}

	private val activeOpModeWrapperCell  = LateInitCell<Wrapper>()

	/**
	 * the currently active OpMode, contents may be undefined if [opModeActive] does not return true
	 */
	@JvmStatic
	var activeOpModeWrapper by activeOpModeWrapperCell

	@JvmStatic
	val activeOpMode: OpMode
		get() { return activeOpModeMirroredCell.get().get() }

	/**
	 * this is mildly expensive to do while an OpMode is running, especially if many features are registered
	 */
	@JvmStatic
	fun registerFeature(feature: Feature) {
		if (registeredFeatures.any { it.get() == feature }) return
		val weakRef = WeakReference(feature)
		registeredFeatures.add(weakRef)
		if (!opModeActive) return
		registrationQueue.add(weakRef to true)
	}

	/**
	 * this is mildly expensive to do while an OpMode is running, especially if many listeners are registered
	 */
	@JvmStatic
	fun deregisterFeature(feature: Feature) {
		registrationQueue.add(WeakReference(feature) to false)
	}

	/**
	 * if this [feature] is attached for [activeOpModeWrapper]
	 */
	@JvmStatic
	fun isAttached(feature: Feature) = activeFeatures.contains(feature)

	private fun resolveRegistrationQueue() {
		if (registrationQueue.isEmpty()) return
		registrationQueue.filter { !it.second }
				.forEach { (feature, _) ->
					RobotLog.vv(TAG, "Deactivating Feature: ${feature::class.java.simpleName}")
					activeFeatures.remove(feature.get())
					registeredFeatures.remove(
							registeredFeatures.first {
								it.get() == feature.get()
							}
					)
				}
		resolveDependencies(
				registrationQueue.filter { it.second }.mapNotNull { it.first.get() }, // makes a copy of the set
				activeFeatures.toSet(),
				activeFlags
		).second.forEach {
			RobotLog.vv(TAG, "Activating Feature: ${it::class.java.simpleName}")
			activeFeatures.add(it)
		}
		registrationQueue.clear()
	}

	/**
	 * ensures that each feature is currently activated, if not, will through a descriptive error about why it isn't
	 *
	 * an optional dependency resolution diagnostic tool
	 */
	@JvmStatic
	fun checkFeatures(opMode: OpMode, vararg features: Feature) {
		val resolved = resolveDependencies(features.toList(), activeFeatures.toSet(), opMode.javaClass.annotations.toList()).first
		// throws all the exceptions it came across in one giant message, if we find any
		if (!features.all { resolved[it].isNullOrEmpty() }) {
			throw DependencyResolutionFailureException(resolved.values.fold(Exception("")) { exception: Exception, featureDependencyResolutionFailureExceptions: Set<FeatureDependencyResolutionFailureException> ->
				exception + featureDependencyResolutionFailureExceptions.fold(Exception("")) { failureException: Exception, featureDependencyResolutionFailureException: FeatureDependencyResolutionFailureException ->
					failureException + featureDependencyResolutionFailureException
				}
			}.message!!)
		}
	}

	private val opModeManagerCell = LateInitCell<OpModeManagerImpl>()
	private var opModeManager by opModeManagerCell

	/**
	 * registers this instance against the event loop, automatically called by the FtcEventLoop, should not be called by the user
	 */
	@OnCreateEventLoop
	@JvmStatic
	fun registerSelf(@Suppress("UNUSED_PARAMETER") context: Context, ftcEventLoop: FtcEventLoop) {
		RobotLog.vv(TAG, "Registering self with event loop")
		opModeManagerCell.safeInvoke {
			RobotLog.vv(TAG, "Previously attached to an OpModeManager, detaching...")
			it.unregisterListener(this)
		}
		opModeManager = ftcEventLoop.opModeManager
		activeOpModeMirroredCell.invalidate()
		RobotLog.vv(TAG, "Attaching to an OpModeManager")
		opModeManager.registerListener(this)
	}

	private fun cleanFeatures() {
		registeredFeatures = registeredFeatures.filter { it.get() != null }.toMutableSet()
	}

	override fun onOpModePreInit(opMode: OpMode) {
		cleanFeatures()

		// locate feature flags, and then populate active listeners
		activeFlags.addAll(opMode.javaClass.annotations)

		registrationQueue.addAll(registeredFeatures.map{ it to true })
		resolveRegistrationQueue()

		val meta = RegisteredOpModes.getInstance().getOpModeMetadata(opModeManager.activeOpModeName) ?: throw RuntimeException("could not find metadata for OpMode")

		// replace the OpMode with a wrapper that the user never sees, but provides our hooks
		activeOpModeWrapper = when (opMode) {
			is LinearOpMode -> {
				LinearOpModeWrapper(opMode, meta)
			}
			else -> {
				val wrapper = OpModeWrapper(opMode, meta)
				activeOpModeMirroredCell.get().accept(wrapper)
				wrapper
			}
		}
		opModeActive = true

		// resolves the queue of anything that was registered later
		resolveRegistrationQueue()

		RobotLog.vv(TAG, "Initing opmode with the following active features:")
		RobotLog.vv(TAG, activeFeatures.map { it.toString() }.toString())
	}

	@JvmStatic
	fun opModePreInit(opMode: Wrapper) {
		if (opMode is OpModeWrapper) opMode.initialiseThings()
		when (opMode) {
			is OpModeWrapper -> opMode._state = Wrapper.OpModeState.INIT
			is LinearOpModeWrapper -> opMode._state = Wrapper.OpModeState.INIT
		}
		resolveRegistrationQueue()
		activeFeatures.forEach { it.preUserInitHook(opMode) }
	}

	@JvmStatic
	fun opModePostInit(opMode: Wrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.postUserInitHook(opMode) }
	}

	@JvmStatic
	fun opModePreInitLoop(opMode: Wrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.preUserInitLoopHook(opMode) }
	}

	@JvmStatic
	fun opModePostInitLoop(opMode: Wrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.postUserInitLoopHook(opMode) }
	}

	@JvmStatic
	fun opModePreStart(opMode: Wrapper) {
		when (opMode) {
			is OpModeWrapper -> opMode._state = Wrapper.OpModeState.ACTIVE
			is LinearOpModeWrapper -> opMode._state = Wrapper.OpModeState.ACTIVE
		}
		resolveRegistrationQueue()
		activeFeatures.forEach { it.preUserStartHook(opMode) }
	}

	override fun onOpModePreStart(opMode: OpMode) {
		// we expose our own hook, rather than this one
	}

	@JvmStatic
	fun opModePostStart(opMode: Wrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.postUserStartHook(opMode) }
	}

	@JvmStatic
	fun opModePreLoop(opMode: Wrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.preUserLoopHook(opMode) }
	}

	@JvmStatic
	fun opModePostLoop(opMode: Wrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.postUserLoopHook(opMode) }
	}

	@JvmStatic
	fun opModePreStop(opMode: Wrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.preUserStopHook(opMode) }
	}

	@JvmStatic
	fun opModePostStop(opMode: Wrapper) {
		resolveRegistrationQueue()
		activeFeatures.forEach { it.postUserStopHook(opMode) }
		when (opMode) {
			is OpModeWrapper -> opMode._state = Wrapper.OpModeState.STOPPED
			is LinearOpModeWrapper -> opMode._state = Wrapper.OpModeState.STOPPED
		}
	}

	override fun onOpModePostStop(opMode: OpMode) {
		// we expose our own hook, rather than this one
		resolveRegistrationQueue()
		// empty active listeners and active flags
		activeFeatures.clear()
		activeFlags.clear()
		opModeActive = false
		System.gc()
	}
}