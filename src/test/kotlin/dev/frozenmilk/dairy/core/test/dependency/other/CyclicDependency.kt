package dev.frozenmilk.dairy.core.test.dependency.other

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeatureClass
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(OpModeTestRunner::class)
class CyclicDependency : TestOpMode() {
	private val features = listOf(
		CyclicA(), CyclicB()
	)
	override fun init() {
		features.forEach {
			if (it.active) Assert.fail("features should not attach")
		}
	}
}

private class CyclicA : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = SingleFeatureClass(CyclicB::class.java)
		.onResolve {
			Assert.fail()
		}
		.onFail {
			Assert.assertEquals("no instances of CyclicB attached", it.message)
		}
}
private class CyclicB : Feature {
	init {
		register()
	}
	override var dependency: Dependency<*> = SingleFeatureClass(CyclicA::class.java)
		.onFail {
			Assert.assertEquals("no instances of CyclicA attached", it.message)
		}
		.onResolve {
			Assert.fail()
		}
}