package dev.frozenmilk.dairy.core.dependency.meta

import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.DependencyBase
import java.util.function.BiFunction
import java.util.function.Supplier

open class LogicalOperator<L, R, OUT>(
		val l: Dependency<L>,
		val r: Dependency<R>,
		operator: BiFunction<Supplier<L>, Supplier<R>, OUT>
) : DependencyBase<OUT>({ opMode, features, yielding ->
	operator.apply(
			{ l.resolve(opMode, features, yielding) },
			{ r.resolve(opMode, features, yielding) }
	)
})