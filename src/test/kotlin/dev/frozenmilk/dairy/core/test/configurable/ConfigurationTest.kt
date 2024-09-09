package dev.frozenmilk.dairy.core.test.configurable

import com.qualcomm.robotcore.hardware.HardwareMap
import dev.frozenmilk.dairy.core.util.configurable.Configurable
import dev.frozenmilk.dairy.core.util.configurable.Configuration
import dev.frozenmilk.dairy.testrt.OpModeTestRunner
import dev.frozenmilk.dairy.testrt.Test
import dev.frozenmilk.dairy.testrt.TestOpMode
import dev.frozenmilk.dairy.testrt.TestOpModeConfiguration
import my.cool.library.configurable.Test3LibraryConfig
import org.firstinspires.ftc.teamcode.configurable.Test4TeamCodeConfig
import org.firstinspires.ftc.teamcode.configurable.Test5TeamCodeConfig
import org.junit.Assert
import org.junit.runner.RunWith

@RunWith(OpModeTestRunner::class)
class ConfigurationTest : TestOpMode() {
	internal var flag8: Configuration<ConfigurationTest>? = null
	override fun init() {
		Assert.assertEquals(listOf(Test1Config1, Test1Config2), Test1.flag)
		Assert.assertEquals(Test2Config2, Test2.flag)
		Assert.assertEquals(Test3LibraryConfig, Test3.flag)
		Assert.assertEquals(Test4TeamCodeConfig, Test4.flag)
		Assert.assertEquals(Test5TeamCodeConfig, Test5.flag)
		Assert.assertEquals(Test6BaseConfig, Test6.flag)
		Assert.assertNotNull(hardwareMap)
		Assert.assertNotNull(telemetry)
		Assert.assertEquals(Test8Config, flag8)
	}
}

// no override same package
private object Test1 : Configurable {
	val flag = mutableListOf<Configuration<Test1>>()
}

// should not be run
private object Test1Config1 : Configuration<Test1> {
	override val configurableClass = Test1::class.java
	override val prioritisedOver: List<Configuration<in Test1>> = emptyList()

	override fun configure(configurable: Test1) {
		configurable.flag.add(this)
	}
}

private object Test1Config2 : Configuration<Test1> {
	override val configurableClass = Test1::class.java
	override val prioritisedOver: List<Configuration<in Test1>> = emptyList()

	override fun configure(configurable: Test1) {
		configurable.flag.add(this)
	}
}

// override by priority
private object Test2 : Configurable {
	var flag: Configuration<Test2>? = null
}

// should not be run
private object Test2Config1 : Configuration<Test2> {
	override val configurableClass = Test2::class.java
	override val prioritisedOver: List<Configuration<in Test2>> = emptyList()

	override fun configure(configurable: Test2) {
		Assert.assertNull(configurable.flag)
		Assert.fail()
		configurable.flag = this
	}
}

private object Test2Config2 : Configuration<Test2> {
	override val configurableClass = Test2::class.java
	override val prioritisedOver: List<Configuration<in Test2>> = listOf(Test2Config1)

	override fun configure(configurable: Test2) {
		Assert.assertNull(configurable.flag)
		configurable.flag = this
	}
}

// override by 'library'
object Test3 : Configurable {
	var flag: Configuration<Test3>? = null
}

// should not be run
private object Test3DairyConfig : Configuration<Test3> {
	override val configurableClass = Test3::class.java
	override val prioritisedOver: List<Configuration<in Test3>> = emptyList()

	override fun configure(configurable: Test3) {
		Assert.assertNull(configurable.flag)
		Assert.fail()
		configurable.flag = this
	}
}

// override by teamcode
object Test4 : Configurable {
	var flag: Configuration<Test4>? = null
}

// should not be run
private object Test4DairyConfig : Configuration<Test4> {
	override val configurableClass = Test4::class.java
	override val prioritisedOver: List<Configuration<in Test4>> = emptyList()

	override fun configure(configurable: Test4) {
		Assert.assertNull(configurable.flag)
		Assert.fail()
		configurable.flag = this
	}
}

// override by library, then teamcode
object Test5 : Configurable {
	var flag: Configuration<Test5>? = null
}

// should not be run
private object Test5DairyConfig : Configuration<Test5> {
	override val configurableClass = Test5::class.java
	override val prioritisedOver: List<Configuration<in Test5>> = emptyList()

	override fun configure(configurable: Test5) {
		Assert.assertNull(configurable.flag)
		Assert.fail()
		configurable.flag = this
	}
}

open class Test6Base : Configurable {
	var flag: Configuration<Test6Base>? = null
}
object Test6 : Test6Base()

private object Test6BaseConfig : Configuration<Test6Base> {
	override val configurableClass = Test6Base::class.java
	override val prioritisedOver: List<Configuration<in Test6Base>> = emptyList()

	override fun configure(configurable: Test6Base) {
		Assert.assertNull(configurable.flag)
		configurable.flag = this
	}
}

// shouldn't run
private object Test7Config : Configuration<Test> {
	override val configurableClass = Test::class.java
	override val prioritisedOver: List<Configuration<in Test>> = listOf()

	override fun configure(configurable: Test) {
		Assert.assertNull(configurable.opMode.hardwareMap)
		Assert.fail()
		configurable.opMode.hardwareMap = HardwareMap(null, null)
	}
}

private object Test8Config : Configuration<ConfigurationTest> {
	override val configurableClass = ConfigurationTest::class.java
	override val prioritisedOver: List<Configuration<in ConfigurationTest>> = listOf(Test7Config, TestOpModeConfiguration.INSTANCE)

	override fun configure(configurable: ConfigurationTest) {
		Assert.assertNull(configurable.opMode.hardwareMap)
		Assert.assertNull(configurable.flag8)
		configurable.flag8 = this
		configurable.opMode.hardwareMap = HardwareMap(null, null)
	}
}
