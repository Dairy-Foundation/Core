package dev.frozenmilk.dairy.core.wrapper

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

sealed interface Wrapper {
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