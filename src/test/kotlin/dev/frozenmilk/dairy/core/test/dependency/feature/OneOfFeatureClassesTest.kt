package dev.frozenmilk.dairy.core.test.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.feature.OneOfFeatureClasses
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(OpModeTestRunner::class)
class OneOfFeatureClassesTest1 : TestOpMode() {
	init {
		AttachOne()
		AttachTwo()
	}
	private val features = listOf(OneOfFeatureClassesFeature(true))
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
class OneOfFeatureClassesTest2 : TestOpMode() {
	private val features = run {
		val one = AttachOne()
		listOf(one, OneOfFeatureClassesFeature(false, one))
	}
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
		val activeFeatures = FeatureRegistrar.activeFeatures
		Assert.assertEquals(
			true,
			activeFeatures.indexOf(features[0]) < activeFeatures.indexOf(features[1])
		)
	}
}

@RunWith(OpModeTestRunner::class)
class OneOfFeatureClassesTest3 : TestOpMode() {
	private val features = run {
		val two = AttachTwo()
		listOf(two, OneOfFeatureClassesFeature(false, two))
	}
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
		val activeFeatures = FeatureRegistrar.activeFeatures
		Assert.assertEquals(
			true,
			activeFeatures.indexOf(features[0]) < activeFeatures.indexOf(features[1])
		)
	}
}

@RunWith(OpModeTestRunner::class)
class OneOfFeatureClassesTest4 : TestOpMode() {
	private val features = listOf(OneOfFeatureClassesFeature(true))
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

private class OneOfFeatureClassesFeature(shouldFail: Boolean, expected: Feature? = null) : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = OneOfFeatureClasses(AttachOne::class.java, AttachTwo::class.java)
		.onResolve {
			Assert.assertEquals("Didn't find expected $it", expected, it)
			if(shouldFail) Assert.fail("$this should fail")
		}
		.onFail {
			if(!shouldFail) Assert.fail("$this should resolve")
		}
}
