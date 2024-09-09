package dev.frozenmilk.dairy.core.test.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeature
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(OpModeTestRunner::class)
class SingleFeatureTest1 : TestOpMode() {
	private val features = run{
		val one = AttachOne()
		listOf(one, SingleFeatureFeature(false, one))
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
class SingleFeatureTest2 : TestOpMode() {
	private val features = run{
		val one = AttachOne()
		one.dependency = Dependency { _, _, _ -> throw DependencyResolutionException("fail") }
		listOf(one, SingleFeatureFeature(true, one))
	}
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

private class SingleFeatureFeature(shouldFail: Boolean, expected: Feature) : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = SingleFeature(expected)
		.onResolve {
			Assert.assertEquals("Didn't find expected $it", expected, it)
			if(shouldFail) Assert.fail("$this should fail")
		}
		.onFail {
			if(!shouldFail) Assert.fail("$this should resolve")
		}
}
