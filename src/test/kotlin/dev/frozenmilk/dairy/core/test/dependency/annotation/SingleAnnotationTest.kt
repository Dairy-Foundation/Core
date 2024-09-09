package dev.frozenmilk.dairy.core.test.dependency.annotation

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotation
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith
import java.lang.annotation.Inherited

@RunWith(OpModeTestRunner::class)
@SingleAnnotationFeature.Attach
class SingleAnnotationTest1 : TestOpMode() {
	private val features = listOf(SingleAnnotationFeature(false))
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
class SingleAnnotationTest2 : TestOpMode() {
	private val features = listOf(SingleAnnotationFeature(true))
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

private class SingleAnnotationFeature(shouldFail: Boolean) : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = SingleAnnotation(Attach::class.java)
		.onResolve {
			if(shouldFail) Assert.fail("$this should fail")
		}
		.onFail {
			if(!shouldFail) Assert.fail("$this should resolve, $it")
		}

	@Target(AnnotationTarget.CLASS)
	@Retention(AnnotationRetention.RUNTIME)
	@Inherited
	annotation class Attach
}
