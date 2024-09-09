package org.firstinspires.ftc.teamcode.configurable

import dev.frozenmilk.dairy.core.test.configurable.Test4
import dev.frozenmilk.dairy.core.util.configurable.Configuration
import org.junit.Assert

object Test4TeamCodeConfig : Configuration<Test4> {
	override val configurableClass = Test4::class.java
	override val prioritisedOver: List<Configuration<in Test4>> = emptyList()

	override fun configure(configurable: Test4) {
		Assert.assertNull(configurable.flag)
		configurable.flag = this
	}
}

