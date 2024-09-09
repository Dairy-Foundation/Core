package dev.frozenmilk.dairy.core.test.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeatureClass
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(OpModeTestRunner::class)
class SingleFeatureClassTest1 : TestOpMode() {
	private val features = run{
		val one = AttachOne()
		listOf(one, SingleFeatureClassFeature(false, AttachOne::class.java, listOf(one)))
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
class SingleFeatureClassTest2 : TestOpMode() {
	private val features = run{
		val one = AttachOne()
		val one2 = AttachOne()
		listOf(one, one2, SingleFeatureClassFeature(false, AttachOne::class.java, listOf(one, one2)))
	}
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
		val activeFeatures = FeatureRegistrar.activeFeatures
		Assert.assertEquals(
			true,
			activeFeatures.indexOf(features[1]) < activeFeatures.indexOf(features[2])
		)
		Assert.assertEquals(
			true,
			activeFeatures.indexOf(features[0]) < activeFeatures.indexOf(features[2])
		)
	}
}

@RunWith(OpModeTestRunner::class)
class SingleFeatureClassTest3 : TestOpMode() {
	private val features = run{
		val one = AttachOne()
		val one2 = AttachOne()
		val one3 = AttachOne()
		listOf(one, one2, one3, SingleFeatureClassFeature(false, AttachOne::class.java, listOf(one, one2, one3)))
	}
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
		val activeFeatures = FeatureRegistrar.activeFeatures
		Assert.assertEquals(
			true,
			activeFeatures.indexOf(features[2]) < activeFeatures.indexOf(features[3])
		)
		Assert.assertEquals(
			true,
			activeFeatures.indexOf(features[1]) < activeFeatures.indexOf(features[3])
		)
		Assert.assertEquals(
			true,
			activeFeatures.indexOf(features[0]) < activeFeatures.indexOf(features[3])
		)
	}
}

@RunWith(OpModeTestRunner::class)
class SingleFeatureClassTest4 : TestOpMode() {
	private val features = run{
		val one = AttachOne()
		one.dependency = Dependency { _, _, _ -> throw DependencyResolutionException("fail") }
		listOf(one, SingleFeatureClassFeature(true, AttachOne::class.java, listOf()))
	}
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("$it should not be attached")
		}
	}
}

private class SingleFeatureClassFeature<T: Feature>(shouldFail: Boolean, expectedClass: Class<T>, expected: List<T>) : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = SingleFeatureClass(expectedClass)
		.onResolve {
			Assert.assertEquals("Didn't find expected $it", expected, it)
			if(shouldFail) Assert.fail("$this should fail")
		}
		.onFail {
			if(!shouldFail) Assert.fail("$this should resolve")
		}
}
