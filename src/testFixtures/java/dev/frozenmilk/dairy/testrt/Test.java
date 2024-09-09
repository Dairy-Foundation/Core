package dev.frozenmilk.dairy.testrt;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.jetbrains.annotations.NotNull;

import dev.frozenmilk.dairy.core.util.configurable.Configurable;

public interface Test extends Configurable {
	default boolean getAdvanceToStart() { return true; }
	default boolean getAdvanceToStop() { return true; }
	@NotNull
	OpMode getOpMode();
}
