package dev.frozenmilk.dairy.core.test.dependency

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding

object YieldingTest : Feature {
	override val dependency = Yielding
}
