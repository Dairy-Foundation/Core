package dev.frozenmilk.dairy.core.util.profile

import dev.frozenmilk.dairy.core.util.supplier.numeric.MotionComponentSupplier

interface MotionProfile<T> : MotionComponentSupplier<T> {
    interface State<T> {
        val position: T
        val velocity: T
        val acceleration: T
    }

    fun getLimited(t: Double): State<T>
    fun getLimited(): State<T>

    fun getLimitless(t: Double): State<T>
    fun getLimitless(): State<T>

    fun get(t: Double): State<T>
    fun get(): State<T>
}