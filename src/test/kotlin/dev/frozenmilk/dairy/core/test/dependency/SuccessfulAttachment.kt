package dev.frozenmilk.dairy.core.test.dependency

import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(OpModeTestRunner::class)
@SingleAnnotationTest.Attach
class SuccessfulAttachment : TestOpMode() {
	override fun getFeatures() = listOf(
			YieldingTest,
			SingleAnnotationTest,
			SingleFeatureTest,
			FailBindingTest,
	)
	override fun init() {
		// all are attached
		features.forEach { Assert.assertEquals(true, it.isAttached()) }
		val activeFeatures = FeatureRegistrar.activeFeatures
		// FailBindingTest attached before YieldingTest
		Assert.assertEquals(true, activeFeatures.indexOf(FailBindingTest) < activeFeatures.indexOf(
			YieldingTest
		))
		// SingleAnnotationTest attached before YieldingTest
		Assert.assertEquals(true, activeFeatures.indexOf(SingleAnnotationTest) < activeFeatures.indexOf(
			YieldingTest
		))
		// YieldingTest attached before SingleFeatureTest
		Assert.assertEquals(true, activeFeatures.indexOf(YieldingTest) < activeFeatures.indexOf(
			SingleFeatureTest
		))
	}
}