package dev.frozenmilk.dairy.core.dependency.resolution

/**
 * @param messages a list of error causing objects paired with error message strings, the object may be left null, to indicate no affiliation to an error causing object
 */
class DependencyResolutionException (val messages: MutableList<Pair<Any?, String>>) : RuntimeException() {
	constructor(messages: Collection<Pair<Any?, String>>) : this (messages.toMutableList())
	constructor(vararg messages: Pair<Any?, String>) : this(mutableListOf(*messages))
	constructor(vararg messages: String) : this(messages.asList())
	constructor(messages: Iterable<String>) : this(messages.associateBy { null }.map { (k, v) -> k to v }.toMutableList())
	@JvmOverloads
	fun addError(obj: Any? = null, message: String) : DependencyResolutionException {
		messages.add(obj to message)
		return this
	}

	override val message
		get() = messages.fold("") { acc, (obj, message) ->
			if (obj != null) "$acc\n$message -- $obj"
			else "$acc\n$message"
		}

	override val cause = null
}