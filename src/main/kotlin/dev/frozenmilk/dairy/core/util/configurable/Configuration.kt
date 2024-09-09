package dev.frozenmilk.dairy.core.util.configurable

import dev.frozenmilk.sinister.Preload

@Preload
interface Configuration<CONFIGURABLE: Configurable> {
	/**
	 * used for runtime reflection
	 */
	val configurableClass: Class<CONFIGURABLE>
	/**
	 * configures [configurable]
	 */
	fun configure(configurable: CONFIGURABLE)
	/**
	 * other configurations that this configuration is meant to override,
	 * will cause them to not be applied, they can be applied manually if need be
	 *
	 * must not contain this
	 */
	val prioritisedOver: List<Configuration<in CONFIGURABLE>>
}