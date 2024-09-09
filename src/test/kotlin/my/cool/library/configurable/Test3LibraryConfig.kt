package my.cool.library.configurable

import dev.frozenmilk.dairy.core.test.configurable.Test3
import dev.frozenmilk.dairy.core.util.configurable.Configuration
import org.junit.Assert

object Test3LibraryConfig : Configuration<Test3> {
	override val configurableClass = Test3::class.java
	override val prioritisedOver: List<Configuration<in Test3>> = emptyList()

	override fun configure(configurable: Test3) {
		Assert.assertNull(configurable.flag)
		configurable.flag = this
	}
}
