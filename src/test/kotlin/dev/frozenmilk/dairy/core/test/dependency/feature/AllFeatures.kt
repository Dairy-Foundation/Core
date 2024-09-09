package dev.frozenmilk.dairy.core.test.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.feature.AllFeatures
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(OpModeTestRunner::class)
class AllFeaturesTest1 : TestOpMode() {
	private val features = run{
		val one = AttachOne()
		val two = AttachTwo()
		listOf(one, two, AllFeaturesFeature(false, one, two))
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
class AllFeaturesTest2 : TestOpMode() {
	private val features = run{
		val one = AttachOne()
		one.dependency = Dependency { _, _, _ -> throw DependencyResolutionException("fail") }
		val two = AttachTwo()
		listOf(one, AllFeaturesFeature(true, one, two))
	}
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
class AllFeaturesTest3 : TestOpMode() {
	private val features = run{
		val one = AttachOne()
		val two = AttachTwo()
		two.dependency = Dependency { _, _, _ -> throw DependencyResolutionException("fail") }
		listOf(two, AllFeaturesFeature(true, one, two))
	}
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
class AllFeaturesTest4 : TestOpMode() {
	private val features = run{
		val one = AttachOne()
		one.dependency = Dependency { _, _, _ -> throw DependencyResolutionException("fail") }
		val two = AttachTwo()
		two.dependency = Dependency { _, _, _ -> throw DependencyResolutionException("fail") }
		listOf(one, two, AllFeaturesFeature(true, one, two))
	}
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

private class AllFeaturesFeature(shouldFail: Boolean, vararg expected: Feature) : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = AllFeatures(*expected)
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
