package dev.frozenmilk.dairy.testrt;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * add to subclasses to run them as tests:
 * <p>
 * `@RunWith(OpModeTestRunner.class)`
 */
public abstract class ExternalTest implements Test {
	@NotNull
	private final OpMode opMode;
	private boolean advanceToStart = true;
	private boolean advanceToStop = true;
	public ExternalTest(@NotNull OpMode opMode) {
		this.opMode = opMode;
	}
	public ExternalTest(@NotNull Class<? extends OpMode> opModeClass) {
		this(instantiate(opModeClass));
	}
	@Override
	public boolean getAdvanceToStart() {
		return this.advanceToStart;
	}
	
	@Override
	public boolean getAdvanceToStop() {
		return this.advanceToStop;
	}
	
	public final void setAdvanceToStart(boolean advanceToStart) {
		this.advanceToStart = advanceToStart;
	}
	
	public final void setAdvanceToStop(boolean advanceToStop) {
		this.advanceToStop = advanceToStop;
	}
	
	@NotNull
	public OpMode getOpMode() {
		return opMode;
	}
	
	@NotNull
	private static OpMode instantiate(@NotNull Class<? extends OpMode> opModeClass) {
		Constructor<? extends OpMode> constructor;
		try {
			constructor = opModeClass.getDeclaredConstructor();
			constructor.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		try {
			return constructor.newInstance();
		} catch (IllegalAccessException | InstantiationException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
	}
}
