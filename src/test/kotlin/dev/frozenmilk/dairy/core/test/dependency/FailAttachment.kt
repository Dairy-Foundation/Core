package dev.frozenmilk.dairy.core.test.dependency

import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.TestOpMode
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(OpModeTestRunner::class)
class FailAttachment : TestOpMode() {
	override fun getFeatures() = listOf(
			SingleAnnotationTest,
			SingleFeatureTest,
	)
	override fun init() {
		features.forEach { Assert.assertEquals(false, it.isAttached()) }
	}
}