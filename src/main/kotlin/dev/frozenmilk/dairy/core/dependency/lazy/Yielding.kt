package dev.frozenmilk.dairy.core.dependency.lazy

import dev.frozenmilk.dairy.core.dependency.DependencyBase
import dev.frozenmilk.dairy.core.dependency.resolution.DependencyResolutionException

object Yielding : DependencyBase<Unit>({ _, _, yielding -> if (!yielding) throw DependencyResolutionException("not yet yielding") })