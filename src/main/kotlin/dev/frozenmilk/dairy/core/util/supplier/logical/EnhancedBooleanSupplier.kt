package dev.frozenmilk.dairy.core.util.supplier.logical

import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.FeatureRegistrar
import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.Supplier

open class EnhancedBooleanSupplier(private val booleanSupplier: Supplier<Boolean>, private val risingDebounce: Long, private val fallingDebounce: Long) : Supplier<Boolean>, Feature{
	constructor(booleanSupplier: Supplier<Boolean>) : this(booleanSupplier, 0, 0)
	private var previous = booleanSupplier.get()
	private var current = booleanSupplier.get()
	@get:JvmName("toggleTrue")
	var toggleTrue = booleanSupplier.get()
		private set
	@get:JvmName("toggleFalse")
	var toggleFalse = booleanSupplier.get()
		private set
	private var timeMarker = 0L
	private fun update() {
		previous = current
		val time = System.nanoTime()
		if(!current && booleanSupplier.get()){
			if(time - timeMarker > risingDebounce) {
				current = true
				toggleTrue = !toggleTrue
				timeMarker = time
			}
		}
		else if (current && !booleanSupplier.get()) {
			if (time - timeMarker > fallingDebounce) {
				current = false
				toggleFalse = !toggleFalse
				timeMarker = time
			}
		}
		else {
			timeMarker = time
		}
	}

	private var valid = false

	/**
	 * causes the next call to [get] to update this supplier
	 */
	fun invalidate() {
		valid = false
	}

	/**
	 * returns the current boolean state of this
	 */
	override fun get(): Boolean {
		if (!valid) {
			update()
			valid = true
		}
		return current
	}

	/**
	 * equivalent to [get]
	 */
	@get:JvmName("state")
	val state: Boolean get() { return get() }

	/**
	 * a rising edge detector for this
	 */
	@get:JvmName("onTrue")
	val onTrue: Boolean get() { return get() && !previous }

	/**
	 * a falling edge detector for this
	 */
	@get:JvmName("onFalse")
	val onFalse: Boolean get() { return !get() && previous }

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to both the rising and falling edges
	 */
	fun debounce(debounce: Double) = EnhancedBooleanSupplier(this.booleanSupplier, (debounce * 1E9).toLong(), (debounce * 1E9).toLong())

	/**
	 * non-mutating
	 *
	 * @param rising is applied to the rising edge
	 * @param falling is applied to the falling edge
	 */
	fun debounce(rising: Double, falling: Double) = EnhancedBooleanSupplier(this.booleanSupplier, (rising * 1E9).toLong(), (falling * 1E9).toLong())

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to the rising edge
	 */
	fun debounceRisingEdge(debounce: Double) = EnhancedBooleanSupplier(this.booleanSupplier, (debounce * 1E9).toLong(), this.fallingDebounce)

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to the falling edge
	 */
	fun debounceFallingEdge(debounce: Double) = EnhancedBooleanSupplier(this.booleanSupplier, this.risingDebounce, (debounce * 1E9).toLong())

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun and(booleanSupplier: Supplier<Boolean>) = EnhancedBooleanSupplier { this.get() and booleanSupplier.get() }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun or(booleanSupplier: Supplier<Boolean>) = EnhancedBooleanSupplier { this.get() or booleanSupplier.get() }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	infix fun xor(booleanSupplier: Supplier<Boolean>) = EnhancedBooleanSupplier { this.get() xor booleanSupplier.get() }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that has the inverse of this, and keeps the debounce information
	 */
	operator fun not() = EnhancedBooleanSupplier ({ !this.booleanSupplier.get() }, risingDebounce, fallingDebounce)

	//
	// Impl Feature:
	//
	override val dependencies = DependencySet(this)
			.yields()

	init {
		register()
	}

	/**
	 * if this automatically updates, by calling [invalidate] and [get]
	 */
	var autoUpdates = true
	private fun autoUpdatePre() {
		if (autoUpdates) {
			get()
		}
	}
	private fun autoUpdatePost() {
		if (autoUpdates) {
			invalidate()
		}
	}

	override fun preUserInitHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserInitHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserInitLoopHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserInitLoopHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserStartHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserStartHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserLoopHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserLoopHook(opMode: Wrapper) = autoUpdatePost()
	override fun preUserStopHook(opMode: Wrapper) = autoUpdatePre()
	override fun postUserStopHook(opMode: Wrapper) {
		deregister()
	}
}