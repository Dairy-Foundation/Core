package dev.frozenmilk.dairy.core.test.dependency

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.feature.SingleFeature

object SingleFeatureTest : Feature {
	override val dependency = SingleFeature(YieldingTest)
}