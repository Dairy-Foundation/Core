package dev.frozenmilk.dairy.testrt;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.jetbrains.annotations.NotNull;

/**
 * add to subclasses to run them as tests:
 * <p>
 * `@RunWith(OpModeTestRunner.class)`
 */
public abstract class TestOpMode extends OpMode implements Test {
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
	
	@Override
	@NotNull
	public final OpMode getOpMode() { return this; }
	
	@Override
	public void init() {}
	
	@Override
	public void loop() {}
}