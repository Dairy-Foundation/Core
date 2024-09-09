package dev.frozenmilk.dairy.core.util.controller.implementation

import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier
import java.util.function.Consumer

@FunctionalInterface
fun interface MotionComponentConsumer<T> : Consumer<MotionComponentSupplier<T>>