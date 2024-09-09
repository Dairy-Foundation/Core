package dev.frozenmilk.dairy.core.util.configurable

interface Configurable {
	fun configure() {
		ConfigurableSinisterFilter.configure(this)
	}
}
