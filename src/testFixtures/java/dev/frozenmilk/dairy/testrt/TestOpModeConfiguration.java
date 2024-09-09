package dev.frozenmilk.dairy.testrt;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import dev.frozenmilk.dairy.core.util.configurable.Configuration;

public class TestOpModeConfiguration implements Configuration<Test> {
	protected TestOpModeConfiguration() {}
	public static final TestOpModeConfiguration INSTANCE = new TestOpModeConfiguration();
	@NotNull
	@Override
	public final Class<Test> getConfigurableClass() {
		return Test.class;
	}
	
	@Override
	public final void configure(@NotNull Test configurable) {
		configureOpMode(configurable.getOpMode());
	}
	
	protected void configureOpMode(@NotNull OpMode opMode) {
		opMode.hardwareMap = new HardwareMap(null, null);
	}
	
	@NotNull
	@Override
	public List<Configuration<? super Test>> getPrioritisedOver() {
		return Collections.emptyList();
	}
}
