package dev.frozenmilk.dairy.core.util.supplier.numeric

fun interface MCSErrorCalculator<T> {
    operator fun invoke(
        targetSupplier: MotionComponentSupplier<out T>,
        stateSupplier: MotionComponentSupplier<out T>,
        motionComponent: MotionComponents
    ): T
}