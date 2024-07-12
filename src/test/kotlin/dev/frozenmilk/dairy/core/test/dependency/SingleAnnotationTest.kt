package dev.frozenmilk.dairy.core.test.dependency

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.annotation.SingleAnnotation
import java.lang.annotation.Inherited

object SingleAnnotationTest : Feature {
	override val dependency = SingleAnnotation(Attach::class.java)

	/**
	 * > SingleAnnotationTest Feature attached
	 */
	@Target(AnnotationTarget.CLASS)
	@Retention(AnnotationRetention.RUNTIME)
	@MustBeDocumented
	@Inherited
	annotation class Attach
}
