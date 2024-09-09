/** @noinspection KotlinInternalInJava */
package dev.frozenmilk.dairy.testrt;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ThreadPool;

import dev.frozenmilk.dairy.core.Feature;
import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.util.configurable.ConfigurableSinisterFilter;
import dev.frozenmilk.dairy.core.wrapper.LinearOpModeWrapper;
import dev.frozenmilk.dairy.core.wrapper.OpModeWrapper;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;
import dev.frozenmilk.sinister.SinisterUtil;
import dev.frozenmilk.util.cell.LateInitCell;
import dev.frozenmilk.util.cell.MirroredCell;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerScheduler;
import org.junit.runners.model.Statement;

public final class OpModeTestRunner extends BlockJUnit4ClassRunner {
	@NotNull
	private final FrameworkMethod testMethod;
	@NotNull
	private final MirroredCell<LinkedHashSet<Feature>> activeFeaturesMirror;
	@NotNull
	private final Runnable cleanFeatures;
	@NotNull
	private final MirroredCell<Queue<Pair<WeakReference<Feature>, Boolean>>> registrationQueueMirror;
	@NotNull
	private final MirroredCell<Set<WeakReference<Feature>>> registeredFeaturesMirror;
	@NotNull
	private final Runnable resolveRegistrationQueue;
	@NotNull
	private final MirroredCell<LateInitCell<Wrapper>> activeOpModeWrapperCellMirror;
	@NotNull
	private final LateInitCell<Wrapper> activeOpModeWrapper$delegate;
	@NotNull
	private final MirroredCell<Boolean> opModeRunningMirror;
	@NotNull
	private final Constructor<LinearOpModeWrapper> linearOpModeWrapperConstructor;
	@NotNull
	private final Constructor<OpModeWrapper> opModeWrapperConstructor;
	@NotNull
	private final MirroredCell<Boolean> logDependencyResolutionFailuresMirror;
	
	/** @noinspection KotlinInternalInJava*/
	@NotNull
	private static Class<?> customClassLoad(@NotNull Class<?> opModeClass) {
		new TestSinister().run();
		ConfigurableSinisterFilter.INSTANCE.onCreateCallback();
		return opModeClass;
	}
	
	@Override
	public void setScheduler(RunnerScheduler scheduler) {
		super.setScheduler(scheduler);
	}
	
	public OpModeTestRunner(@NotNull Class<?> opModeClass) throws NoSuchMethodException, InitializationError, ClassNotFoundException {
		super(customClassLoad(opModeClass));
		final Method runWrapper = OpModeTestRunner.class.getDeclaredMethod("runWrapper", Wrapper.class, Test.class);
		runWrapper.setAccessible(true);
		this.testMethod = new FrameworkMethod(runWrapper);
		this.activeFeaturesMirror = new MirroredCell<>(FeatureRegistrar.INSTANCE, "_activeFeatures");
		final Method cleanFeatures = SinisterUtil.getAllMethods(FeatureRegistrar.class, m -> m.getName().equals("cleanFeatures")).get(0);
		cleanFeatures.setAccessible(true);
		this.cleanFeatures = () -> {
			try {
				cleanFeatures.invoke(FeatureRegistrar.INSTANCE);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getCause());
			}
		};
		this.registrationQueueMirror = new MirroredCell<>(FeatureRegistrar.INSTANCE, "registrationQueue");
		this.registeredFeaturesMirror = new MirroredCell<>(FeatureRegistrar.INSTANCE, "_registeredFeatures");
		final Method resolveRegistrationQueue = SinisterUtil.getAllMethods(FeatureRegistrar.class, m -> m.getName().equals("resolveRegistrationQueue")).get(0);
		resolveRegistrationQueue.setAccessible(true);
		this.resolveRegistrationQueue = () -> {
			try {
				resolveRegistrationQueue.invoke(FeatureRegistrar.INSTANCE);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getCause());
			}
		};
		this.activeOpModeWrapperCellMirror = new MirroredCell<>(FeatureRegistrar.INSTANCE, "activeOpModeWrapperCell");
		this.activeOpModeWrapper$delegate = this.getActiveOpModeWrapperCell();
		this.opModeRunningMirror = new MirroredCell<>(FeatureRegistrar.INSTANCE, "opModeRunning");
		this.linearOpModeWrapperConstructor = LinearOpModeWrapper.class.getDeclaredConstructor(LinearOpMode.class, OpModeMeta.class);
		Intrinsics.checkNotNullExpressionValue(this.linearOpModeWrapperConstructor, "getDeclaredConstructor(...)");
		this.opModeWrapperConstructor = OpModeWrapper.class.getDeclaredConstructor(OpMode.class, OpModeMeta.class);
		Intrinsics.checkNotNullExpressionValue(this.opModeWrapperConstructor, "getDeclaredConstructor(...)");
		this.linearOpModeWrapperConstructor.setAccessible(true);
		this.opModeWrapperConstructor.setAccessible(true);
		this.logDependencyResolutionFailuresMirror = new MirroredCell<>(FeatureRegistrar.INSTANCE, "logDependencyResolutionFailures");
	}
	
	//
	// Runner Logic
	//
	
	@NotNull
	protected List<FrameworkMethod> computeTestMethods() {
		return CollectionsKt.mutableListOf(this.testMethod);
	}
	
	protected void validateTestMethods(@Nullable List errors) {
	}
	
	//
	// Getters and Setters
	//
	
	private LinkedHashSet<Feature> getActiveFeatures() {
		return this.activeFeaturesMirror.get();
	}
	
	private void setActiveFeatures(LinkedHashSet<Feature> var1) {
		this.activeFeaturesMirror.accept(var1);
	}
	
	@NotNull
	protected List<TestRule> getTestRules(@Nullable Object target) {
		List<TestRule> testRules = super.getTestRules(target);
		testRules.add(0, (base, description) -> dairyOpModeRuntime(target, base));
		return testRules;
	}
	
	private Queue<Pair<WeakReference<Feature>, Boolean>> getRegistrationQueue() {
		return this.registrationQueueMirror.get();
	}
	
	private Set<WeakReference<Feature>> getRegisteredFeatures() {
		return this.registeredFeaturesMirror.get();
	}
	
	private LateInitCell<Wrapper> getActiveOpModeWrapperCell() {
		return this.activeOpModeWrapperCellMirror.get();
	}
	
	private Wrapper getActiveOpModeWrapper() {
		return this.activeOpModeWrapper$delegate.get();
	}
	
	private void setActiveOpModeWrapper(Wrapper var1) {
		this.activeOpModeWrapper$delegate.accept(var1);
	}
	
	private void setOpModeRunning(boolean var1) {
		this.opModeRunningMirror.accept(var1);
	}
	
	public void patchFeatureRegistrar(@NotNull OpMode opMode) throws Throwable {
		Intrinsics.checkNotNullParameter(opMode, "opMode");
		this.cleanFeatures.run();
		OpModeMeta.Builder var10000 = new OpModeMeta.Builder();
		String var10001 = opMode.getClass().getSimpleName();
		OpModeMeta meta = var10000.setName(var10001).build();
		Object var4;
		Object[] var5;
		//noinspection rawtypes
		Constructor var17;
		Wrapper var18;
		if (opMode instanceof LinearOpMode) {
			var17 = this.linearOpModeWrapperConstructor;
		} else {
			var17 = this.opModeWrapperConstructor;
		}
		var5 = new Object[]{opMode, meta};
		var4 = var17.newInstance(var5);
		Intrinsics.checkNotNull(var4);
		var18 = (Wrapper)var4;
		
		this.setActiveOpModeWrapper(var18);
		logDependencyResolutionFailuresMirror.accept(SinisterUtil.inheritsAnnotation(opMode.getClass(), FeatureRegistrar.LogDependencyResolutionExceptions.class));
		this.setOpModeRunning(true);
		Queue<Pair<WeakReference<Feature>, Boolean>> var16 = this.getRegistrationQueue();
		Set<WeakReference<Feature>> $this$map$iv = this.getRegisteredFeatures();
		ArrayList<Pair<WeakReference<Feature>, Boolean>> destination$iv$iv = new ArrayList<>($this$map$iv.size());
		
		for (WeakReference<Feature> it : $this$map$iv) {
			Pair<WeakReference<Feature>, Boolean> var14 = TuplesKt.to(it, true);
			destination$iv$iv.add(var14);
		}
		
		var16.addAll(destination$iv$iv);
		this.resolveRegistrationQueue.run();
	}
	
	@NotNull
	@Contract("null, _ -> fail")
	private Statement dairyOpModeRuntime(final Object target, final Statement base) {
		if (!(target instanceof Test)) {
			throw new IllegalStateException();
		} else {
			return new Statement() {
				public void evaluate() throws Throwable {
					Test test = (Test) target;
					test.configure();
					
					patchFeatureRegistrar(test.getOpMode());
					try {
						base.evaluate();
					}
					finally {
						try {
							FeatureRegistrar.INSTANCE.onOpModePostStop(test.getOpMode());
						}
						finally {
							for (WeakReference<Feature> wFef : getRegisteredFeatures()) {
								Feature feature = wFef.get();
								if (feature != null) feature.deregister();
							}
							resolveRegistrationQueue.run();
						}
					}
				}
			};
		}
	}
	
	@NotNull
	@Contract("_, _ -> new")
	@Override
	protected Statement methodInvoker(FrameworkMethod method, Object test) {
		return new InvokeRunWrapperTestStatement(method, (Test) test);
	}
	
//	@Override
//	public void run(final RunNotifier notifier) {
//		Runnable runnable = () -> super.run(notifier);
//		Thread thread = new Thread(runnable);
//		thread.setContextClassLoader(sinisterClassLoaderCell.get());
//		thread.start();
//		try {
//			thread.join();
//		} catch (InterruptedException e) {
//			throw new RuntimeException(e);
//		}
//	}
//
	private static class InvokeRunWrapperTestStatement extends Statement {
		private final FrameworkMethod testMethod;
		private final Test test;
		
		public InvokeRunWrapperTestStatement(FrameworkMethod testMethod, Test test) {
			this.testMethod = testMethod;
			this.test = test;
		}
		
		@Override
		public void evaluate() throws Throwable {
			testMethod.invokeExplosively(null, FeatureRegistrar.getActiveOpModeWrapper(), test);
		}
	}
	
	private static boolean resolveRegistrationQueue$lambda$5$lambda$4(@NotNull Method it) {
		return Intrinsics.areEqual(it.getName(), "resolveRegistrationQueue");
	}
	
	private static void runWrapper(@NotNull Wrapper wrapper, @NotNull Test test) {
		if (wrapper instanceof OpModeWrapper) {
			runIterativeOpMode((OpModeWrapper) wrapper, test);
		}
		else if (wrapper instanceof LinearOpModeWrapper) {
			runLinearOpMode((LinearOpModeWrapper) wrapper, test);
		}
		else {
			throw new IllegalStateException("Wrapper must be one of these");
		}
	}
	
	private static void runLinearOpMode(@NotNull LinearOpModeWrapper wrapper, @NotNull Test test) {
		final Method internalStart = SinisterUtil.getAllMethods(LinearOpMode.class, m -> m.getName().equals("internalStart")).get(0);
		internalStart.setAccessible(true);
		final Field stopRequested = SinisterUtil.getAllFields(LinearOpMode.class, f -> f.getName().equals("stopRequested")).get(0);
		stopRequested.setAccessible(true);
		ExecutorService opModeThread = Executors.newSingleThreadExecutor();
		opModeThread.submit(() -> {
			try {
				System.out.print("\n\n---OpMode Init---\n\n\n");
				wrapper.getOpMode().runOpMode();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
		try {
			//noinspection StatementWithEmptyBody
			while (!test.getAdvanceToStart()) {}
			System.out.print("\n\n---OpMode Start---\n\n\n");
			internalStart.invoke(wrapper.getOpMode());
			//noinspection StatementWithEmptyBody
			while (!test.getAdvanceToStop()) {}
			System.out.print("\n\n---OpMode Stop---\n\n\n");
			stopRequested.set(wrapper.getOpMode(), true);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
	}
	
	private static void runIterativeOpMode(@NotNull OpModeWrapper wrapper, @NotNull Test test) {
		System.out.print("\n\n---OpMode Init---\n\n\n");
		wrapper.init();
		
		do {
			wrapper.init_loop();
		}
		while (!test.getAdvanceToStart());
		
		System.out.print("\n\n---OpMode Start---\n\n\n");
		wrapper.start();
		
		do {
			wrapper.loop();
		}
		while (!test.getAdvanceToStop());
		
		System.out.print("\n\n---OpMode Stop---\n\n\n");
		wrapper.stop();
	}
}
