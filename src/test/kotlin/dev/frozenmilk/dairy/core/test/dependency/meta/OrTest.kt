package dev.frozenmilk.dairy.core.test.dependency.meta

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotation
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith
import java.lang.annotation.Inherited

@RunWith(OpModeTestRunner::class)
@OrFeature.AttachOne
@OrFeature.AttachTwo
class OrTest1 : TestOpMode() {
	private val features = listOf(OrFeature(false))
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
@OrFeature.AttachOne
class OrTest2 : TestOpMode() {
	private val features = listOf(OrFeature(false))
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
@OrFeature.AttachTwo
class OrTest3 : TestOpMode() {
	private val features = listOf(OrFeature(false))
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
class OrTest4 : TestOpMode() {
	private val features = listOf(OrFeature(true))
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

private class OrFeature(shouldFail: Boolean) : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = (
			SingleAnnotation(AttachOne::class.java) or
					SingleAnnotation(AttachTwo::class.java)
			)
		.onResolve {
			if(shouldFail) Assert.fail("$this should fail")
		}
		.onFail {
			if(!shouldFail) Assert.fail("$this should resolve")
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
