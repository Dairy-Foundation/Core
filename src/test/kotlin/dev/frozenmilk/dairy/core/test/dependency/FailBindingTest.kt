package dev.frozenmilk.dairy.core.test.dependency

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import org.junit.Assert

object FailBindingTest : Feature {
	override val dependency = Dependency { _, _, _ -> }.upgrade()
			.onFail {
				Assert.fail()
			}
}
