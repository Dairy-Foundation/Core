package dev.frozenmilk.dairy.core.wrapper

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta

class LinearOpModeWrapper internal constructor(override val opMode: LinearOpMode, override val meta: OpModeMeta) : Wrapper {
	internal var _state = Wrapper.OpModeState.STOPPED
	override val opModeType: OpModeMeta.Flavor = meta.flavor
	override val state: Wrapper.OpModeState
		get() { return _state }
	override val name: String = meta.displayName
}