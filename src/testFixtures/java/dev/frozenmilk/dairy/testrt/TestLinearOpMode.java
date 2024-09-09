package dev.frozenmilk.dairy.testrt;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.jetbrains.annotations.NotNull;

/**
 * add to subclasses to run them as tests:
 * <p>
 * `@RunWith(OpModeTestRunner.class)`
 */
public abstract class TestLinearOpMode extends LinearOpMode implements Test {
	private boolean advanceToStart = true;
	private boolean advanceToStop = true;
	
	@Override
	public final boolean getAdvanceToStart() {
		return this.advanceToStart;
	}
	
	@Override
	public final boolean getAdvanceToStop() {
		return this.advanceToStop;
	}
	
	public final void setAdvanceToStart(boolean advanceToStart) {
		this.advanceToStart = advanceToStart;
	}
	
	public final void setAdvanceToStop(boolean advanceToStop) {
		this.advanceToStop = advanceToStop;
	}
	
	@NotNull
	@Override
	public final OpMode getOpMode() { return this; }
}
