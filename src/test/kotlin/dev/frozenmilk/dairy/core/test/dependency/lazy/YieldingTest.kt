package dev.frozenmilk.dairy.core.test.dependency.lazy

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(OpModeTestRunner::class)
class YieldingTest1 : TestOpMode() {
	private val features = listOf(YieldingFeature())
	override fun init() {
		features.forEach {
			if (!it.active) Assert.fail("$it should be attached")
		}
	}
}

@RunWith(OpModeTestRunner::class)
class YieldingTest2 : TestOpMode() {
	private val features = listOf(DefaultFeature(), YieldingFeature())
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


private class YieldingFeature : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = Yielding
}

private class DefaultFeature : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = Dependency{ _, _, _ ->  }
}