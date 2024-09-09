package dev.frozenmilk.dairy.core.util.configurable

import android.content.Context
import com.qualcomm.robotcore.util.RobotLog
import dev.frozenmilk.sinister.SinisterFilter
import dev.frozenmilk.sinister.apphooks.OnCreate
import dev.frozenmilk.sinister.staticInstancesOf
import dev.frozenmilk.sinister.targeting.EmptySearch
import dev.frozenmilk.sinister.targeting.WideSearch

internal object ConfigurableSinisterFilter : SinisterFilter, OnCreate {
	override val targets = WideSearch()
	private val dairySearch = EmptySearch().include("dev.frozenmilk")
	private val teamCodeSearch = EmptySearch().include("org.firstinspires.ftc.teamcode")

	private val configurations = mutableListOf<Configuration<*>>()
	private val configurables = mutableListOf<Configurable>()
	override fun filter(clazz: Class<*>) {
		configurables.addAll(clazz.staticInstancesOf(Configurable::class.java))
		configurations.addAll(clazz.staticInstancesOf(Configuration::class.java))
	}

	private fun Configuration<*>.detectCycle(): Boolean {
		return prioritisedOver.any { it == this@detectCycle } || prioritisedOver.any { it.detectCycle(
			this@detectCycle
		) }
	}

	private fun Configuration<*>.detectCycle(toCheck: Configuration<*>): Boolean {
		return prioritisedOver.any { it == toCheck } || prioritisedOver.any { it.detectCycle(toCheck) }
	}

	override fun onCreate(context: Context) {
		onCreateCallback()
	}

	fun onCreateCallback() {
		configurations.removeIf {
			val res = it.detectCycle()
			if (res) RobotLog.ee("Configuration", "cycle detected during configuration collection for $it")
			res
		}
		configurables.forEach {
			it.configure()
		}
		configurables.clear()
	}

	enum class Level {
		DAIRY,
		LIBRARY,
		TEAMCODE
	}

	@Suppress("UNCHECKED_CAST")
	fun <CONFIGURABLE: Configurable> configure(configurable: CONFIGURABLE) {
		val cls = configurable.javaClass
		val classConfigurations: Map<Level, List<Configuration<CONFIGURABLE>>> = configurations
			.filter { it.configurableClass.isAssignableFrom(cls) }
			.groupBy {
				if (teamCodeSearch.determineInclusion(it.javaClass.name)) Level.TEAMCODE
				else if (dairySearch.determineInclusion(it.javaClass.name)) Level.DAIRY
				else Level.LIBRARY
			} as Map<Level, List<Configuration<CONFIGURABLE>>>

		classConfigurations.getOrElse(Level.TEAMCODE) {
			classConfigurations.getOrElse(Level.LIBRARY) {
				classConfigurations.getOrElse(Level.DAIRY) {
					emptyList()
				}
			}
		}
			.reduceOrNull { l, r ->
				return@reduceOrNull if (l.recursePrioritisedOver(r)) l
				else if (r.recursePrioritisedOver(l)) r
				else {
					RobotLog.ee("Configuration", "Found two competing configurations: $l, $r\nThis may cause issues, will apply $l, then $r, try removing the conflicting configurations, or applying your own configuration that prioritises itself over these to resolve this issue.")
					object : Configuration<CONFIGURABLE> {
						override val configurableClass: Class<CONFIGURABLE> = l.configurableClass
						override val prioritisedOver: List<Configuration<in CONFIGURABLE>> = listOf(l, r)
						override fun configure(configurable: CONFIGURABLE) {
							l.configure(configurable)
							r.configure(configurable)
						}
					}
				}
			}
			?.also {
				try {
					it.configure(configurable)
				}
				catch (e: Throwable) {
					RobotLog.ee("Configuration", "thrown while applying $it to $configurable:\n%s", e)
				}
			}
	}

	private fun <CONFIGURABLE: Configurable> Configuration<in CONFIGURABLE>.recursePrioritisedOver(r: Configuration<CONFIGURABLE>): Boolean = prioritisedOver.contains(r) || prioritisedOver.any { it.recursePrioritisedOver(r) }
}