package dev.frozenmilk.dairy.core

import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.sinister.SinisterFilter
import dev.frozenmilk.sinister.staticInstancesOf
import dev.frozenmilk.sinister.targeting.WideSearch

private object FeatureSinisterFilter : SinisterFilter {
	override val targets = WideSearch()

	override fun init() {}

	override fun filter(clazz: Class<*>) {
		clazz.staticInstancesOf(Feature::class.java)
				.forEach {
					RobotLog.vv("DairyCore > FeatureSinisterFilter", "found feature instance: ${it::class.java.simpleName}")
					FeatureRegistrar.registerFeature(it)
				}
	}
}