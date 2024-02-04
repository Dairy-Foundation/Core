package dev.frozenmilk.dairy.core.wrapper

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import dev.frozenmilk.dairy.core.FeatureRegistrar
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta.Flavor

class OpModeWrapper internal constructor(override val opMode: OpMode, override val meta: OpModeMeta) : OpMode(), Wrapper {
	override val opModeType: Flavor = meta.flavor

	internal var _state = Wrapper.OpModeState.STOPPED
	override val state: Wrapper.OpModeState
		get() { return _state }

	override val name = meta.displayName

	/**
	 * moves things around, so that the irritating little fields that exist on each OpMode get remapped through this correctly
	 *
	 * since this wrapper gets made AFTER the OpMode gets made and has a bunch of info passed to it, its mainly just pulling things up into this
	 */
	internal fun initialiseThings() {
		this.hardwareMap = opMode.hardwareMap
		this.telemetry = opMode.telemetry

		this.gamepad1 = opMode.gamepad1
		this.gamepad2 = opMode.gamepad2

		_state = Wrapper.OpModeState.INIT
	}

	override fun init() {
		FeatureRegistrar.opModePreInit(this)
		opMode.init()
		FeatureRegistrar.opModePostInit(this)
	}

	override fun init_loop() {
		FeatureRegistrar.opModePreInitLoop(this)
		opMode.init_loop()
		FeatureRegistrar.opModePostInitLoop(this)
	}

	override fun start() {
		FeatureRegistrar.opModePreStart(this)
		opMode.start()
		FeatureRegistrar.opModePostStart(this)
	}

	override fun loop() {
		FeatureRegistrar.opModePreLoop(this)
		opMode.loop()
		FeatureRegistrar.opModePostLoop(this)
	}

	override fun stop() {
		FeatureRegistrar.opModePreStop(this)
		opMode.stop()
		FeatureRegistrar.opModePostStop(this)
	}
}
