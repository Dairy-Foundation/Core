package dev.frozenmilk.dairy.core.test.dependency.feature

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding

class AttachOne : Feature {
	override var dependency: Dependency<*> = Yielding
	init {
		register()
	}
}
class AttachTwo : Feature {
	override var dependency: Dependency<*> = Yielding
	init {
		register()
	}
}
