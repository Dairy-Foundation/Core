package dev.frozenmilk.dairy.core.dependency.meta

import dev.frozenmilk.dairy.core.dependency.Dependency

@Suppress("NAME_SHADOWING")
class Or <L, R> (l: Dependency<L>, r: Dependency<R>) : LogicalOperator<L, R, Pair<L?, R?>>(l, r, { l, r ->
	try {
		l.get() to null
	}
	catch (e: Throwable) {
		null to r.get()
	}
}) {
	override fun accept(p0: Pair<L?, R?>) {
		val (first, second) = p0
		if (first != null) l.accept(first)
		if (second != null) r.accept(second)
		super.accept(p0)
	}

	override fun acceptErr(p0: Throwable) {
		l.acceptErr(p0)
		r.acceptErr(p0)
		super.acceptErr(p0)
	}
}