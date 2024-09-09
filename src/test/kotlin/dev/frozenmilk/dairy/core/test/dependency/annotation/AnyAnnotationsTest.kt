package dev.frozenmilk.dairy.core.test.dependency.annotation

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.annotation.AnyAnnotations
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith
import java.lang.annotation.Inherited

@RunWith(OpModeTestRunner::class)
@AnyAnnotationsFeature.AttachOne
@AnyAnnotationsFeature.AttachTwo
class AnyAnnotationsTest1 : TestOpMode() {
	private val features = listOf(AnyAnnotationsFeature(false))
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
@AnyAnnotationsFeature.AttachOne
class AnyAnnotationsTest2 : TestOpMode() {
	private val features = listOf(AnyAnnotationsFeature(false))
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
@AnyAnnotationsFeature.AttachTwo
class AnyAnnotationsTest3 : TestOpMode() {
	private val features = listOf(AnyAnnotationsFeature(false))
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
class AnyAnnotationsTest4 : TestOpMode() {
	private val features = listOf(AnyAnnotationsFeature(true))
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

private class AnyAnnotationsFeature(shouldFail: Boolean) : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = AnyAnnotations(AttachOne::class.java, AttachTwo::class.java)
		.onResolve {
			if(shouldFail) Assert.fail("$this should fail")
		}
		.onFail {
			if(!shouldFail) Assert.fail("$this should resolve, $it")
		}

	@Target(AnnotationTarget.CLASS)
	@Retention(AnnotationRetention.RUNTIME)
	@Inherited
	annotation class AttachOne
	@Target(AnnotationTarget.CLASS)
	@Retention(AnnotationRetention.RUNTIME)
	@Inherited
	annotation class AttachTwo
}
