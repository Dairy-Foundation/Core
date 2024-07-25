package dev.frozenmilk.dairy.core.test.dependency

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeature
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith

private data object CyclicA : Feature {
	override val dependency = SingleFeature(CyclicB)
		.onResolve {
			Assert.fail()
		}
		.onFail {
			println("$this: ${it.message}")
		}
}
private data object CyclicB : Feature {
	override val dependency = SingleFeature(CyclicA)
		.onFail {
			println("$this: ${it.message}")
		}
		.onResolve {
			Assert.fail()
		}
}

@RunWith(OpModeTestRunner::class)
class CyclicDependency : TestOpMode(CyclicA, CyclicB) {
	init {
		features.forEach {
			if (it.isAttached()) Assert.fail("features should not attach")
		}
	}
}