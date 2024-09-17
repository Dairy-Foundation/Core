package dev.frozenmilk.dairy.core.util.profile

interface MotionProfile<T> {
    interface State<T> {
        val position: T
        val velocity: T
        val acceleration: T
    }

    operator fun get(t: Double): State<T>
    fun get(): State<T>

    fun getEndless(t: Double): State<T>
    fun getEndless(): State<T>
}