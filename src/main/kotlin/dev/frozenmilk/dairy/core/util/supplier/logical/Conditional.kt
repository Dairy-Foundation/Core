package dev.frozenmilk.dairy.core.util.supplier.logical

import org.jetbrains.annotations.Contract
import java.util.function.Supplier

class Conditional<T: Comparable<T>> private constructor(private val supplier: Supplier<T>, private val domainCheckers: List<(T) -> Boolean> = emptyList(), private val domainClosureBuilder: DomainClosureBuilder<T> = DomainClosureBuilder(), private val previousOperationType: OperationType? = null) : IConditional<T> {
	constructor(supplier: Supplier<T>) : this(supplier, emptyList())


	/**
	 * non-mutating
	 */
	override fun lessThan(value: T): Conditional<T> {
		val pair = handleBuildState(OperationType.LESS, Inclusivity.NOT_INCLUSIVE, value)
		var domainClosureBuilder = pair.first
		domainClosureBuilder = domainClosureBuilder.lessThan(value)
		val domainCheckers = pair.second
		return Conditional(supplier, domainCheckers, domainClosureBuilder, OperationType.LESS)
	}

	/**
	 * non-mutating
	 */
	override fun lessThanEqualTo(value: T): Conditional<T> {
		val pair = handleBuildState(OperationType.LESS, Inclusivity.INCLUSIVE, value)
		var domainClosureBuilder = pair.first
		domainClosureBuilder = domainClosureBuilder.lessThanEqualTo(value)
		val domainCheckers = pair.second
		return Conditional(supplier, domainCheckers, domainClosureBuilder, OperationType.LESS)
	}

	/**
	 * non-mutating
	 */
	override fun greaterThan(value: T): Conditional<T> {
		val pair = handleBuildState(OperationType.GREATER, Inclusivity.NOT_INCLUSIVE, value)
		var domainClosureBuilder = pair.first
		domainClosureBuilder = domainClosureBuilder.greaterThan(value)
		val domainCheckers = pair.second
		return Conditional(supplier, domainCheckers, domainClosureBuilder, OperationType.GREATER)
	}
	// when we do a new operation, check to see if it can form a valid closure with the previous operation, if so, perform the closure union, else, close the previous closure and add this one in
	// closes if upper > lower
	// only need to check if we currently already have one domain set
	/**
	 * non-mutating
	 */
	override fun greaterThanEqualTo(value: T): Conditional<T> {
		val pair = handleBuildState(OperationType.GREATER, Inclusivity.INCLUSIVE, value)
		var domainClosureBuilder = pair.first
		domainClosureBuilder = domainClosureBuilder.greaterThanEqualTo(value)
		val domainCheckers = pair.second
		return Conditional(supplier, domainCheckers, domainClosureBuilder, OperationType.GREATER)
	}

	override fun bind(): EnhancedBooleanSupplier {
		var conditional = this
		if (domainClosureBuilder.valid()) {
			conditional = Conditional(supplier, domainCheckers.plus(domainClosureBuilder.build()), domainClosureBuilder, previousOperationType)
		}

		// todo simplify domain checkers by checking their extremes and seeing if one entirely contains another or if two could be merged?
		// doesn't matter for the moment, but very plausible for later
		return EnhancedBooleanSupplier {
			var result = false
			for (domainChecker in conditional.domainCheckers) {
				result = result or domainChecker.invoke(conditional.supplier.get())
			}
			result
		}
	}

	// we should perform a build if:
	// * we already performed an operation of this sign (less / greater)
	// * we already have one value loaded in there AND:
	// * the new value doesn't close, so we actually want inverse values, which we achieve by building the previous value and letting the user continue to cook
	// * OTHERWISE: if the new value DOES close, we add it and then run a build
	private fun handleBuildState(operationType: OperationType, inclusivity: Inclusivity, newValue: T): Pair<DomainClosureBuilder<T>, List<(T) -> Boolean>> {
		var result = domainClosureBuilder to domainCheckers
		if (previousOperationType == operationType || domainClosureBuilder.handleBuildState(newValue, operationType, inclusivity.isInclusive)) {
			result = DomainClosureBuilder<T>() to domainCheckers.plus(domainClosureBuilder.build())
		}
		return result
	}
}
internal enum class OperationType {
	LESS,
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

	private fun compareNull(nullBound: T?, comparator: T, comparison: (T, T) -> Boolean) = if (nullBound == null) true else comparison(comparator, nullBound)
	@Contract(pure = true)
	fun build(): (T) -> Boolean {
		return { value: T ->
			var result = compareNull(lower, value) { v, b -> v > b } && compareNull(upper, value) { v, b -> v < b }
			result = result || (lowerInclusive.isInclusive && value == lower)
			result = result || (upperInclusive.isInclusive && value == upper)
			result
		}
	}

	internal fun handleBuildState(newValue: T, operationType: OperationType, inclusivity: Boolean): Boolean {
		return when (operationType) {
			OperationType.LESS -> {
				compareNull(upper, newValue) { v, b -> v < b } && inclusivity || compareNull(upper, newValue) { v, b -> v <= b } && !inclusivity
			}
			OperationType.GREATER -> {
				compareNull(upper, newValue) { v, b -> v > b } && inclusivity || compareNull(upper, newValue) { v, b -> v >= b } && !inclusivity
			}
		}
	}
	internal fun valid() = lower != null || upper != null
}