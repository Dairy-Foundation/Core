package dev.frozenmilk.dairy.core.dependency.meta

import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.DependencyBase

class And <L, R>(l: Dependency<L>, r: Dependency<R>) : LogicalOperator<L, R, Pair<L, R>>(l, r, { l, r -> l.get() to r.get() }) {
	override fun accept(p0: Pair<L, R>) {
		if (l is DependencyBase<L>) l.accept(p0.first)
		if (r is DependencyBase<R>) r.accept(p0.second)
		super.accept(p0)
	}
	override fun acceptErr(p0: Throwable) {
		if (l is DependencyBase<L>) l.acceptErr(p0)
		if (r is DependencyBase<R>) r.acceptErr(p0)
		super.acceptErr(p0)
	}
}