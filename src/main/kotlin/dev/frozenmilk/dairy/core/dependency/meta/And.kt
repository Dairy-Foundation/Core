package dev.frozenmilk.dairy.core.dependency.meta

import dev.frozenmilk.dairy.core.dependency.Dependency

@Suppress("NAME_SHADOWING")
class And <L, R>(l: Dependency<L>, r: Dependency<R>) : LogicalOperator<L, R, Pair<L, R>>(l, r, { l, r -> l.get() to r.get() }) {
	override fun accept(p0: Pair<L, R>) {
		l.accept(p0.first)
		r.accept(p0.second)
		super.accept(p0)
	}

	override fun acceptErr(p0: Throwable) {
		l.acceptErr(p0)
		r.acceptErr(p0)
		super.acceptErr(p0)
	}
}