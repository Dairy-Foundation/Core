package dev.frozenmilk.dairy.core.wrapper

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import dev.frozenmilk.dairy.core.FeatureRegistrar
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

sealed interface Wrapper {
	/**
	 * only valid for the duration of the runtime of this Wrapper as this is sugar access to the [FeatureRegistrar.activeFeatures] of the current OpMode
	 */
	val activeFeatures
		get() = FeatureRegistrar.activeFeatures
	val inheritedAnnotations: List<Annotation>
	val opMode: OpMode
	val meta: OpModeMeta
	val opModeType: OpModeMeta.Flavor

	enum class OpModeState {
		/**
		 * in [OpMode.init] or [OpMode.init_loop]
		 */
		INIT,

		/**
		 * in [OpMode.start], [OpMode.loop] or [OpMode.stop]
		 */
		ACTIVE,

		/**
		 * inactive
		 */
		STOPPED,
	}

	val state: OpModeState
	val name: String
}