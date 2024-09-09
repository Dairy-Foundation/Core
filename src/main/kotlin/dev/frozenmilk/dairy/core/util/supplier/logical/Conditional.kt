package dev.frozenmilk.dairy.core.util.supplier.logical

import org.jetbrains.annotations.Contract
import java.util.function.BooleanSupplier
import java.util.function.Supplier

class Conditional<T: Comparable<T>> private constructor(private val supplier: Supplier<T>, private val domainCheckers: List<(T) -> Boolean> = emptyList(), private val domainClosureBuilder: DomainClosureBuilder<T> = DomainClosureBuilder()) : IConditional<T> {
	constructor(supplier: Supplier<T>) : this(supplier, emptyList())

	/**
	 * non-mutating
	 */
	override fun lessThan(value: T): Conditional<T> {
		val (domainClosureBuilder, domainCheckers) = handleBuildState(OperationType.LESSER)
		return Conditional(supplier, domainCheckers, domainClosureBuilder.lessThan(value))
	}

	/**
	 * non-mutating
	 */
	override fun lessThanEqualTo(value: T): Conditional<T> {
		val (domainClosureBuilder, domainCheckers) = handleBuildState(OperationType.LESSER)
		return Conditional(supplier, domainCheckers, domainClosureBuilder.lessThanEqualTo(value))
	}

	/**
	 * non-mutating
	 */
	override fun greaterThan(value: T): Conditional<T> {
		val (domainClosureBuilder, domainCheckers) = handleBuildState(OperationType.GREATER)
		return Conditional(supplier, domainCheckers, domainClosureBuilder.greaterThan(value))
	}
	// when we do a new operation, check to see if it can form a valid closure with the previous operation, if so, perform the closure union, else, close the previous closure and add this one in
	// closes if upper > lower
	// only need to check if we currently already have one domain set
	/**
	 * non-mutating
	 */
	override fun greaterThanEqualTo(value: T): Conditional<T> {
		val (domainClosureBuilder, domainCheckers) = handleBuildState(OperationType.GREATER)
		return Conditional(supplier, domainCheckers, domainClosureBuilder.greaterThanEqualTo(value))
	}

	override fun bind(): EnhancedBooleanSupplier {
		var conditional = this
		if (domainClosureBuilder.valid()) {
			conditional = Conditional(supplier, domainCheckers.plus(domainClosureBuilder.build()), domainClosureBuilder)
		}

		// todo simplify domain checkers by checking their extremes and seeing if one entirely contains another or if two could be merged?
		// doesn't matter for the moment, but very plausible for later
		return EnhancedBooleanSupplier {
			val toCheck = conditional.supplier.get()
			conditional.domainCheckers.forEach {
				if (it.invoke(toCheck)) return@EnhancedBooleanSupplier true
			}
			false
		}
	}

	// we should perform a build if:
	// * we already performed an operation of this sign (less / greater)
	// * we already have one value loaded in there AND:
	// * the new value doesn't close, so we actually want inverse values, which we achieve by building the previous value and letting the user continue to cook
	// * OTHERWISE: if the new value DOES close, we add it and then run a build
	private fun handleBuildState(operationType: OperationType): Pair<DomainClosureBuilder<T>, List<(T) -> Boolean>> {
		return if (domainClosureBuilder.wouldErase(operationType)) {
			DomainClosureBuilder<T>() to domainCheckers.plus(domainClosureBuilder.build())
		}
		else domainClosureBuilder to domainCheckers
	}
}
internal enum class OperationType {
	LESSER,
	GREATER
}

internal enum class Inclusivity(val isInclusive: Boolean) {
	INCLUSIVE(true),
	NOT_INCLUSIVE(false)
}

class DomainClosureBuilder<T: Comparable<T>> internal constructor(private val lower: T? = null, private val lowerInclusive: Inclusivity = Inclusivity.INCLUSIVE, private val upper: T? = null, private val upperInclusive: Inclusivity = Inclusivity.INCLUSIVE) {
	@Contract("_ -> new")
	fun lessThan(value: T): DomainClosureBuilder<T> {
		return DomainClosureBuilder(lower, lowerInclusive, value, Inclusivity.NOT_INCLUSIVE)
	}

	@Contract("_ -> new")
	fun lessThanEqualTo(value: T): DomainClosureBuilder<T> {
		return DomainClosureBuilder(lower, lowerInclusive, value, Inclusivity.INCLUSIVE)
	}

	@Contract("_ -> new")
	fun greaterThan(value: T): DomainClosureBuilder<T> {
		return DomainClosureBuilder(value, Inclusivity.NOT_INCLUSIVE, upper, upperInclusive)
	}

	@Contract("_ -> new")
	fun greaterThanEqualTo(value: T): DomainClosureBuilder<T> {
		return DomainClosureBuilder(value, Inclusivity.INCLUSIVE, upper, upperInclusive)
	}

	@Contract(pure = true)
	fun build(): (T) -> Boolean {
		if (lower != null) {
			if (upper != null) {
				if (lowerInclusive.isInclusive) {
					if (upperInclusive.isInclusive) return { it >= lower && it <= upper }
					else return { it >= lower && it < upper }
				}
				else {
					if (upperInclusive.isInclusive) return { it > lower && it <= upper }
					else return { it > lower && it < upper }
				}
			}
			else {
				if (lowerInclusive.isInclusive) {
					return { it >= lower }
				}
				else {
					return { it > lower }
				}
			}
		}
		else if (upper != null) {
			if (upperInclusive.isInclusive) {
				return { it <= upper }
			}
			else {
				return { it < upper }
			}
		}
		return { true }
	}

	/**
	 * should return true if:
	 *
	 * this next operation would erase some information
	 */
	internal fun wouldErase(operationType: OperationType): Boolean {
		return when (operationType) {
			OperationType.LESSER -> {
				upper != null
			}
			OperationType.GREATER -> {
				lower != null
			}
		}
	}
	internal fun valid() = lower != null || upper != null
}