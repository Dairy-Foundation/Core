package dev.frozenmilk.dairy.core.dependency.meta

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.BiFunction
import java.util.function.Supplier

open class LogicalOperator<L, R, OUT>(
		val l: Dependency<L>,
		val r: Dependency<R>,
		val operator: BiFunction<Supplier<L>, Supplier<R>, OUT>
) : Dependency<OUT> {
	override fun resolve(opMode: Wrapper, resolvedFeatures: List<Feature>, yielding: Boolean) =
		operator.apply(
			{ l.resolve(opMode, resolvedFeatures, yielding) },
			{ r.resolve(opMode, resolvedFeatures, yielding) }
		)
}