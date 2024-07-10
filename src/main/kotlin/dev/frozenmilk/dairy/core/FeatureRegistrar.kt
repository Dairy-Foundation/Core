package dev.frozenmilk.dairy.core

import android.content.Context
import com.qualcomm.ftccommon.FtcEventLoop
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier
import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import dev.frozenmilk.dairy.core.dependency.resolution.resolveDependencies
import dev.frozenmilk.dairy.core.wrapper.LinearOpModeWrapper
import dev.frozenmilk.dairy.core.wrapper.OpModeWrapper
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import dev.frozenmilk.sinister.apphooks.OnCreateEventLoop
import dev.frozenmilk.util.cell.LateInitCell
import dev.frozenmilk.util.cell.LazyCell
import dev.frozenmilk.util.cell.MirroredCell
import org.firstinspires.ftc.robotcore.internal.opmode.RegisteredOpModes
import java.lang.ref.WeakReference
import java.util.ArrayDeque
import java.util.Queue
import kotlin.concurrent.Volatile

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
	 * internal repr:
	 *
	 * features that have been activated via [resolveDependencies]
	 */
	private val _activeFeatures = LinkedHashSet<Feature>()

	/**
	 * creates a list of features that have been activated via [resolveDependencies]
	 */
	val activeFeatures
		get() = _activeFeatures.toList()

	/**
	 * if there is currently an [OpMode] active on the robot, should be true almost all of the time
	 */
	@JvmStatic
	@Volatile
	var opModeActive: Boolean = false
		private set

	@JvmStatic
	val opModeState: Wrapper.OpModeState
		get() = activeOpModeWrapper.state

	/**
	 * the mirror cell used to manage this
	 */
	private val activeOpModeMirroredCell = LazyCell {
		MirroredCell<OpMode>(opModeManager, "activeOpMode")
	}

	private val activeOpModeWrapperCell = LateInitCell<Wrapper>()

	/**
	 * the currently active OpMode, contents may be undefined if [opModeActive] does not return true
	 */
	@JvmStatic
	var activeOpModeWrapper by activeOpModeWrapperCell
		private set

	@JvmStatic
	val activeOpMode: OpMode
		get() { return activeOpModeWrapper.opMode }

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
	fun isAttached(feature: Feature) = _activeFeatures.contains(feature)

	private fun resolveRegistrationQueue() {
		if (registrationQueue.isEmpty()) return
		registrationQueue.filter { !it.second }
				.forEach { (feature, _) ->
					RobotLog.vv(TAG, "Deactivating Feature: ${feature::class.java.simpleName}")
					_activeFeatures.remove(feature.get())
					registeredFeatures.remove(
							registeredFeatures.first {
								it.get() == feature.get()
							}
					)
				}
		val toResolve = registrationQueue.filter { it.second }.mapNotNull { it.first.get() } // makes a copy of the set
		val resolved = _activeFeatures.toMutableSet()
		resolveDependencies(
				activeOpModeWrapper,
				toResolve.toMutableSet(),
				resolved,
		)
		resolved.intersect(toResolve.toSet()).forEach {
			RobotLog.vv(TAG, "Activating Feature: ${it::class.java.simpleName}")
			_activeFeatures.add(it)
		}
		registrationQueue.clear()
	}

	/**
	 * ensures that each feature could be activated, if not, will throw a descriptive error about why it isn't
	 *
	 * an optional dependency resolution diagnostic tool
	 *
	 * todo: log the output of errors on normal dependency resolution
	 */
	@JvmStatic
	fun checkFeatures(vararg features: Feature) {
		throw DependencyResolutionException(
				resolveDependencies(
						activeOpModeWrapper,
						features.toMutableSet(),
						_activeFeatures.toMutableSet(),
				)
						.mapNotNull { (k, v) -> k to (v?.message ?: return@mapNotNull null) }
		)
	}

	private val opModeManagerCell = LateInitCell<OpModeManagerImpl>()
	private var opModeManager by opModeManagerCell

	/**
	 * registers this instance against the event loop, automatically called by the FtcEventLoop
	 */
	@Suppress("unused")
	private object OnCreateEventLoopHook : OnCreateEventLoop {
		override fun onCreateEventLoop(context: Context, ftcEventLoop: FtcEventLoop) {
			RobotLog.vv(TAG, "Registering self with event loop")
			opModeManagerCell.safeInvoke {
				RobotLog.vv(TAG, "Previously attached to an OpModeManager, detaching...")
				it.unregisterListener(FeatureRegistrar)
			}
			opModeManager = ftcEventLoop.opModeManager
			activeOpModeMirroredCell.invalidate()
			RobotLog.vv(TAG, "Attaching to an OpModeManager")
			opModeManager.registerListener(FeatureRegistrar)
		}
	}

	private fun cleanFeatures() {
		registeredFeatures = registeredFeatures.filter { it.get() != null }.toMutableSet()
	}

	override fun onOpModePreInit(opMode: OpMode) {
		cleanFeatures()

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

		registrationQueue.addAll(registeredFeatures.map{ it to true })
		resolveRegistrationQueue()

		RobotLog.vv(TAG, "Initing opmode with the following active features:")
		RobotLog.vv(TAG, _activeFeatures.map { it.toString() }.toString())
	}

	@JvmStatic
	fun opModePreInit(opMode: Wrapper) {
		if (opMode is OpModeWrapper) opMode.initialiseThings()
		when (opMode) {
			is OpModeWrapper -> opMode._state = Wrapper.OpModeState.INIT
			is LinearOpModeWrapper -> opMode._state = Wrapper.OpModeState.INIT
		}
		resolveRegistrationQueue()
		_activeFeatures.forEach { it.preUserInitHook(opMode) }
	}

	@JvmStatic
	fun opModePostInit(opMode: Wrapper) {
		resolveRegistrationQueue()
		_activeFeatures.forEach { it.postUserInitHook(opMode) }
	}

	@JvmStatic
	fun opModePreInitLoop(opMode: Wrapper) {
		resolveRegistrationQueue()
		_activeFeatures.forEach { it.preUserInitLoopHook(opMode) }
	}

	@JvmStatic
	fun opModePostInitLoop(opMode: Wrapper) {
		resolveRegistrationQueue()
		_activeFeatures.forEach { it.postUserInitLoopHook(opMode) }
	}

	@JvmStatic
	fun opModePreStart(opMode: Wrapper) {
		when (opMode) {
			is OpModeWrapper -> opMode._state = Wrapper.OpModeState.ACTIVE
			is LinearOpModeWrapper -> opMode._state = Wrapper.OpModeState.ACTIVE
		}
		resolveRegistrationQueue()
		_activeFeatures.forEach { it.preUserStartHook(opMode) }
	}

	override fun onOpModePreStart(opMode: OpMode) {
		// we expose our own hook, rather than this one
	}

	@JvmStatic
	fun opModePostStart(opMode: Wrapper) {
		resolveRegistrationQueue()
		_activeFeatures.forEach { it.postUserStartHook(opMode) }
	}

	@JvmStatic
	fun opModePreLoop(opMode: Wrapper) {
		resolveRegistrationQueue()
		_activeFeatures.forEach { it.preUserLoopHook(opMode) }
	}

	@JvmStatic
	fun opModePostLoop(opMode: Wrapper) {
		resolveRegistrationQueue()
		_activeFeatures.forEach { it.postUserLoopHook(opMode) }
	}

	@JvmStatic
	fun opModePreStop(opMode: Wrapper) {
		resolveRegistrationQueue()
		_activeFeatures.forEach { it.preUserStopHook(opMode) }
	}

	@JvmStatic
	fun opModePostStop(opMode: Wrapper) {
		resolveRegistrationQueue()
		_activeFeatures.forEach { it.postUserStopHook(opMode) }
		when (opMode) {
			is OpModeWrapper -> opMode._state = Wrapper.OpModeState.STOPPED
			is LinearOpModeWrapper -> opMode._state = Wrapper.OpModeState.STOPPED
		}
	}

	override fun onOpModePostStop(opMode: OpMode) {
		// we expose our own hook, rather than this one
		resolveRegistrationQueue()
		// empty active listeners and active flags
		_activeFeatures.clear()
		opModeActive = false
		System.gc()
	}
}