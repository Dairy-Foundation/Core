package dev.frozenmilk.dairy.core.test.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.feature.AnyFeatures
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(OpModeTestRunner::class)
class AnyFeaturesTest1 : TestOpMode() {
	private val features = run{
		val one = AttachOne()
		val two = AttachTwo()
		listOf(one, two, AnyFeaturesFeature(false, one, two))
	}
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
		val activeFeatures = FeatureRegistrar.activeFeatures
		Assert.assertEquals(
			true,
			activeFeatures.indexOf(features[0]) < activeFeatures.indexOf(features[2])
		)
		Assert.assertEquals(
			true,
			activeFeatures.indexOf(features[1]) < activeFeatures.indexOf(features[2])
		)
	}
}

@RunWith(OpModeTestRunner::class)
class AnyFeaturesTest2 : TestOpMode() {
	private val features = run{
		val one = AttachOne()
		listOf(one, AnyFeaturesFeature(false, one))
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
class AnyFeaturesTest3 : TestOpMode() {
	private val features = run{
		val two = AttachTwo()
		listOf(two, AnyFeaturesFeature(false, two))
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
class AnyFeaturesTest4 : TestOpMode() {
	private val features = listOf(AnyFeaturesFeature(true))
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

private class AnyFeaturesFeature(shouldFail: Boolean, vararg expected: Feature) : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = AnyFeatures(*expected)
		.onResolve { found ->
			expected.forEach {
				Assert.assertTrue("Didn't find expected $it", found.contains(it))
			}
			if(shouldFail) Assert.fail("$this should fail")
		}
		.onFail {
			if(!shouldFail) Assert.fail("$this should resolve")
		}
}
