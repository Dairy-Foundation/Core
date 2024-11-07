package dev.frozenmilk.dairy.core

import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.staticInstancesOf
import dev.frozenmilk.sinister.targeting.WideSearch

private object ServiceScanner : Scanner {
	private const val TAG = "ServiceScanner"
	override val targets = WideSearch()

	override fun scan(cls: Class<*>) {
		cls.staticInstancesOf(Feature::class.java)
				.forEach {
					RobotLog.vv(TAG, "registering found feature instance: ${it::class.java.simpleName}")
					FeatureRegistrar.registerFeature(it)
				}
	}

	override fun unload(cls: Class<*>) {
		cls.staticInstancesOf(Feature::class.java)
			.forEach {
				RobotLog.vv(TAG, "unloading found feature instance: ${it::class.java.simpleName}")
				FeatureRegistrar.deregisterFeature(it)
			}
	}
}