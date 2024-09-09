package dev.frozenmilk.dairy.core.test.dependency.annotation

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotations
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith
import java.lang.annotation.Inherited

@RunWith(OpModeTestRunner::class)
@SingleAnnotationsFeature.Attach
class SingleAnnotationsTest1 : TestOpMode() {
	private val features = listOf(SingleAnnotationsFeature(false, 1))
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
@SingleAnnotationsFeature.Attach
@SingleAnnotationsFeature.Attach
class SingleAnnotationsTest2 : TestOpMode() {
	private val features = listOf(SingleAnnotationsFeature(false, 2))
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
@SingleAnnotationsFeature.Attach
@SingleAnnotationsFeature.Attach
@SingleAnnotationsFeature.Attach
class SingleAnnotationsTest3 : TestOpMode() {
	private val features = listOf(SingleAnnotationsFeature(false, 3))
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
class SingleAnnotationsTest4 : TestOpMode() {
	private val features = listOf(SingleAnnotationsFeature(true, 0))
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

private class SingleAnnotationsFeature(shouldFail: Boolean, expectedAnnotations: Int) : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = SingleAnnotations(Attach::class.java)
		.onResolve {
			Assert.assertEquals("mismatched number of expected annotations", expectedAnnotations, it.size)
			if (shouldFail) Assert.fail("$this should fail")
		}
		.onFail {
			if(!shouldFail) Assert.fail("$this should resolve, ${it.stackTraceToString()}")
		}

	@Target(AnnotationTarget.CLASS)
	@Retention(AnnotationRetention.RUNTIME)
	@Repeatable
	@Inherited
	annotation class Attach
}
