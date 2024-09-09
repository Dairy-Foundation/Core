package my.cool.library.configurable

import dev.frozenmilk.dairy.core.test.configurable.Test5
import dev.frozenmilk.dairy.core.util.configurable.Configuration
import org.junit.Assert

object Test5LibraryConfig : Configuration<Test5> {
	override val configurableClass = Test5::class.java
	override val prioritisedOver: List<Configuration<in Test5>> = emptyList()

	override fun configure(configurable: Test5) {
		Assert.assertNull(configurable.flag)
		configurable.flag = this
	}
}
