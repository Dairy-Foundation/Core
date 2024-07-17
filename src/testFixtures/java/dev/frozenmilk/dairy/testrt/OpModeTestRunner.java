package dev.frozenmilk.dairy.testrt;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import dev.frozenmilk.dairy.core.Feature;
import dev.frozenmilk.dairy.core.FeatureRegistrar;
import dev.frozenmilk.dairy.core.wrapper.LinearOpModeWrapper;
import dev.frozenmilk.dairy.core.wrapper.OpModeWrapper;
import dev.frozenmilk.dairy.core.wrapper.Wrapper;
import dev.frozenmilk.sinister.SinisterUtil;
import dev.frozenmilk.util.cell.LateInitCell;
import dev.frozenmilk.util.cell.MirroredCell;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import kotlin.Pair;
import kotlin.TuplesKt;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public final class OpModeTestRunner extends BlockJUnit4ClassRunner {
	@NotNull
	private final FrameworkMethod testMethod;
	@NotNull
	private final MirroredCell<LinkedHashSet<Feature>> activeFeatures$delegate;
	@NotNull
	private final Runnable cleanFeatures;
	@NotNull
	private final MirroredCell<Queue<Pair<WeakReference<Feature>, Boolean>>> registrationQueue$delegate;
	@NotNull
	private final MirroredCell<Set<WeakReference<Feature>>> registeredFeatures$delegate;
	@NotNull
	private final Runnable resolveRegistrationQueue;
	@NotNull
	private final MirroredCell<LateInitCell<Wrapper>> activeOpModeWrapperCell$delegate;
	@NotNull
	private final LateInitCell<Wrapper> activeOpModeWrapper$delegate;
	@NotNull
	private final MirroredCell<Boolean> opModeActive$delegate;
	@NotNull
	private final Constructor<LinearOpModeWrapper> linearOpModeWrapperConstructor;
	@NotNull
	private final Constructor<OpModeWrapper> opModeWrapperConstructor;
	
	public OpModeTestRunner(@NotNull Class opModeClass) throws NoSuchMethodException, InitializationError {
		super(opModeClass);
		this.testMethod = new FrameworkMethod(TestOpMode.class.getDeclaredMethod("runOpMode"));
		this.activeFeatures$delegate = new MirroredCell<>(FeatureRegistrar.INSTANCE, "_activeFeatures");
		final Method cleanFeatures = SinisterUtil.getAllMethods(FeatureRegistrar.class, OpModeTestRunner::cleanFeatures$lambda$3$lambda$2).get(0);
		cleanFeatures.setAccessible(true);
		this.cleanFeatures = () -> {
			try {
				cleanFeatures.invoke(FeatureRegistrar.INSTANCE);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		};
		this.registrationQueue$delegate = new MirroredCell<>(FeatureRegistrar.INSTANCE, "registrationQueue");
		this.registeredFeatures$delegate = new MirroredCell<>(FeatureRegistrar.INSTANCE, "registeredFeatures");
		final Method resolveRegistrationQueue = SinisterUtil.getAllMethods(FeatureRegistrar.class, OpModeTestRunner::resolveRegistrationQueue$lambda$5$lambda$4).get(0);
		resolveRegistrationQueue.setAccessible(true);
		this.resolveRegistrationQueue = (() -> {
			try {
				resolveRegistrationQueue.invoke(FeatureRegistrar.INSTANCE);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		});
		this.activeOpModeWrapperCell$delegate = new MirroredCell<>(FeatureRegistrar.INSTANCE, "activeOpModeWrapperCell");
		this.activeOpModeWrapper$delegate = this.getActiveOpModeWrapperCell();
		this.opModeActive$delegate = new MirroredCell<>(FeatureRegistrar.INSTANCE, "opModeActive");
		this.linearOpModeWrapperConstructor = LinearOpModeWrapper.class.getDeclaredConstructor(LinearOpMode.class, OpModeMeta.class);
		Intrinsics.checkNotNullExpressionValue(this.linearOpModeWrapperConstructor, "getDeclaredConstructor(...)");
		this.opModeWrapperConstructor = OpModeWrapper.class.getDeclaredConstructor(OpMode.class, OpModeMeta.class);
		Intrinsics.checkNotNullExpressionValue(this.opModeWrapperConstructor, "getDeclaredConstructor(...)");
		this.linearOpModeWrapperConstructor.setAccessible(true);
		this.opModeWrapperConstructor.setAccessible(true);
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
		return this.activeFeatures$delegate.get();
	}
	
	private void setActiveFeatures(LinkedHashSet<Feature> var1) {
		this.activeFeatures$delegate.accept(var1);
	}
	
	@NotNull
	protected List<TestRule> getTestRules(@Nullable Object target) {
		List<TestRule> testRules = super.getTestRules(target);
		testRules.add(0, (base, description) -> dairyOpModeRuntime(target, this, base, description));
		return testRules;
	}
	
	private final Queue<Pair<WeakReference<Feature>, Boolean>> getRegistrationQueue() {
		return this.registrationQueue$delegate.get();
	}
	
	private final Set<WeakReference<Feature>> getRegisteredFeatures() {
		return this.registeredFeatures$delegate.get();
	}
	
	private final LateInitCell<Wrapper> getActiveOpModeWrapperCell() {
		return this.activeOpModeWrapperCell$delegate.get();
	}
	
	private final Wrapper getActiveOpModeWrapper() {
		return this.activeOpModeWrapper$delegate.get();
	}
	
	private final void setActiveOpModeWrapper(Wrapper var1) {
		this.activeOpModeWrapper$delegate.accept(var1);
	}
	
	private final boolean getOpModeActive() {
		return this.opModeActive$delegate.get();
	}
	
	private final void setOpModeActive(boolean var1) {
		this.opModeActive$delegate.accept(var1);
	}
	
	public final void patchFeatureRegistrar(@NotNull OpMode opMode) throws InvocationTargetException, IllegalAccessException, InstantiationException {
		Intrinsics.checkNotNullParameter(opMode, "opMode");
		this.cleanFeatures.run();
		OpModeMeta.Builder var10000 = new OpModeMeta.Builder();
		String var10001 = Reflection.getOrCreateKotlinClass(opMode.getClass()).getSimpleName();
		Intrinsics.checkNotNull(var10001);
		assert var10001 != null;
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
		this.setOpModeActive(true);
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
	
	private static Statement dairyOpModeRuntime(final Object $target, final OpModeTestRunner this$0, final Statement base, Description var3) {
		Intrinsics.checkNotNullParameter(this$0, "this$0");
		if (!($target instanceof TestOpMode)) {
			throw new IllegalStateException();
		} else {
			return new Statement() {
				public void evaluate() throws Throwable {
					Iterable<Feature> $this$forEach$iv = ((TestOpMode)$target).features;
					Iterator<Feature> var3 = $this$forEach$iv.iterator();
					
					Feature element$iv;
					Feature it;
					while(var3.hasNext()) {
						element$iv = var3.next();
						it = element$iv;
						it.register();
					}
					
					this$0.patchFeatureRegistrar((OpMode)$target);
					base.evaluate();
					FeatureRegistrar.INSTANCE.onOpModePostStop((OpMode)$target);
					$this$forEach$iv = this$0.getActiveFeatures();
					var3 = $this$forEach$iv.iterator();
					
					while(var3.hasNext()) {
						element$iv = var3.next();
						it = element$iv;
						it.deregister();
					}
				}
			};
		}
	}
	
	private static final boolean cleanFeatures$lambda$3$lambda$2(Method it) {
		Intrinsics.checkNotNullParameter(it, "it");
		return Intrinsics.areEqual(it.getName(), "cleanFeatures");
	}
	
	private static final boolean resolveRegistrationQueue$lambda$5$lambda$4(Method it) {
		Intrinsics.checkNotNullParameter(it, "it");
		return Intrinsics.areEqual(it.getName(), "resolveRegistrationQueue");
	}
}
