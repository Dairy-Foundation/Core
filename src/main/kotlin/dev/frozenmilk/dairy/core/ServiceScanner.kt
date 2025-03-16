package dev.frozenmilk.dairy.core

import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.sinister.Scanner
import dev.frozenmilk.sinister.staticInstancesOf
import dev.frozenmilk.sinister.targeting.WideSearch

@Suppress("unused")
private object ServiceScanner : Scanner {
	private val TAG = javaClass.simpleName
	override val loadAdjacencyRule = Scanner.INDEPENDENT
	override val unloadAdjacencyRule = Scanner.INDEPENDENT
	override val targets = WideSearch()
	private val serviceMap = mutableMapOf<ClassLoader, MutableList<Feature>>()

	override fun beforeScan(loader: ClassLoader) {
		serviceMap[loader] = mutableListOf()
	}
	override fun scan(loader: ClassLoader, cls: Class<*>) {
		cls.staticInstancesOf(Feature::class.java)
			.forEach {
				RobotLog.vv(TAG, "registering found feature instance: ${it::class.java.simpleName}")
				serviceMap[loader]!!.add(it)
				FeatureRegistrar.registerFeature(it)
			}
	}

	override fun beforeUnload(loader: ClassLoader) {
		serviceMap.remove(loader)?.forEach {
			RobotLog.vv(TAG, "unloading found feature instance: ${it::class.java.simpleName}")
			FeatureRegistrar.deregisterFeature(it)
		}
	}
	override fun unload(loader: ClassLoader, cls: Class<*>) {}
}