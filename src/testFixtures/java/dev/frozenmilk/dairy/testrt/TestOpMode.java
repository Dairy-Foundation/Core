package dev.frozenmilk.dairy.testrt;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import dev.frozenmilk.dairy.core.Feature;
import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.wrapper.OpModeWrapper;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;

/**
 * add to subclasses to run them as tests:
 * <p>
 * `@RunWith(OpModeTestRunner.class)`
 */
public abstract class TestOpMode extends OpMode {
	private boolean advanceToStart = true;
	private boolean advanceToStop = true;
	
	@NotNull
	public Collection<Feature> features;
	
	protected TestOpMode(@NotNull Collection<Feature> features) {
		this.features = features;
	}
	
	protected TestOpMode(@NotNull Feature... features) {
		this.features = Arrays.asList(features);
	}
	
	public boolean getAdvanceToStart() {
		return this.advanceToStart;
	}
	
	public void setAdvanceToStart(boolean var1) {
		this.advanceToStart = var1;
	}
	
	public void init() {
	}
	
	public void loop() {
	}
	
	public boolean getAdvanceToStop() {
		return this.advanceToStop;
	}
	
	public void setAdvanceToStop(boolean var1) {
		this.advanceToStop = var1;
	}
	
	public final void runOpMode() {
		Wrapper opMode = FeatureRegistrar.getActiveOpModeWrapper();
		if (!(opMode instanceof OpModeWrapper)) {
			throw new RuntimeException("TestRunner cannot handle LinearOpModes");
		} else {
			((OpModeWrapper)opMode).init();
			
			do {
				((OpModeWrapper)opMode).init_loop();
			}
			while(!this.getAdvanceToStart());
			
			((OpModeWrapper)opMode).start();
			
			do {
				((OpModeWrapper)opMode).loop();
			}
			while(!this.getAdvanceToStop());
			
			((OpModeWrapper)opMode).stop();
		}
	}
}
