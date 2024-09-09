package dev.frozenmilk.dairy.core.test.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.feature.OneOfFeatures
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(OpModeTestRunner::class)
class OneOfFeaturesTest1 : TestOpMode() {
	private val features = run {
		val one = AttachOne()
		val two = AttachTwo()
		listOf(OneOfFeaturesFeature(true, null, one, two))
	}
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
class OneOfFeaturesTest2 : TestOpMode() {
	private val features = run {
		val one = AttachOne()
		listOf(one, OneOfFeaturesFeature(false, one, one))
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
class OneOfFeaturesTest3 : TestOpMode() {
	private val features = run {
		val two = AttachTwo()
		listOf(two, OneOfFeaturesFeature(false, two, two))
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
class OneOfFeaturesTest4 : TestOpMode() {
	private val features = listOf(OneOfFeaturesFeature(true, null))
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

private class OneOfFeaturesFeature(shouldFail: Boolean, expected: Feature?, vararg features: Feature) : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = OneOfFeatures(*features)
		.onResolve {
			Assert.assertEquals("Didn't find expected $it", expected, it)
			if(shouldFail) Assert.fail("$this should fail")
		}
		.onFail {
			if(!shouldFail) Assert.fail("$this should resolve")
		}
}
