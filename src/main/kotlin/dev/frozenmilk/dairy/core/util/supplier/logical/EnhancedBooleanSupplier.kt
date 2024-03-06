package dev.frozenmilk.dairy.core.util.supplier.logical

import dev.frozenmilk.dairy.core.dependencyresolution.dependencyset.DependencySet
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.Supplier
@Suppress("INAPPLICABLE_JVM_NAME")
open class EnhancedBooleanSupplier(private val booleanSupplier: Supplier<Boolean>, private val risingDebounce: Long, private val fallingDebounce: Long) : IEnhancedBooleanSupplier {
	constructor(booleanSupplier: Supplier<Boolean>) : this(booleanSupplier, 0, 0)
	private var previous = booleanSupplier.get()
	private var current = previous
	private var _toggleTrue = current
	@get:JvmName("toggleTrue")
	override val toggleTrue
		get() = _toggleTrue
	private var _toggleFalse = current
	@get:JvmName("toggleFalse")
	override val toggleFalse
		get() = _toggleFalse
	private var timeMarker = 0L
	private fun update() {
		previous = current
		val time = System.nanoTime()
		if(!current && booleanSupplier.get()){
			if(time - timeMarker > risingDebounce) {
				current = true
				_toggleTrue = !_toggleTrue
				timeMarker = time
			}
		}
		else if (current && !booleanSupplier.get()) {
			if (time - timeMarker > fallingDebounce) {
				current = false
				_toggleFalse = !_toggleFalse
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
	override fun invalidate() {
		valid = false
	}

	/**
	 * returns the current boolean state of this
	 */
	@get:JvmName("state")
	override val state: Boolean get() {
		if (!valid) {
			update()
			valid = true
		}
		return current
	}

	/**
	 * a rising edge detector for this
	 */
	@get:JvmName("onTrue")
	override val onTrue: Boolean get() { return state && !previous }

	/**
	 * a falling edge detector for this
	 */
	@get:JvmName("onFalse")
	override val onFalse: Boolean get() { return !state && previous }

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to both the rising and falling edges
	 */
	override fun debounce(debounce: Double) = EnhancedBooleanSupplier(this.booleanSupplier, (debounce * 1E9).toLong(), (debounce * 1E9).toLong())

	/**
	 * non-mutating
	 *
	 * @param rising is applied to the rising edge
	 * @param falling is applied to the falling edge
	 */
	override fun debounce(rising: Double, falling: Double) = EnhancedBooleanSupplier(this.booleanSupplier, (rising * 1E9).toLong(), (falling * 1E9).toLong())

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to the rising edge
	 */
	override fun debounceRisingEdge(debounce: Double) = EnhancedBooleanSupplier(this.booleanSupplier, (debounce * 1E9).toLong(), this.fallingDebounce)

	/**
	 * non-mutating
	 *
	 * @param debounce is applied to the falling edge
	 */
	override fun debounceFallingEdge(debounce: Double) = EnhancedBooleanSupplier(this.booleanSupplier, this.risingDebounce, (debounce * 1E9).toLong())

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	override infix fun and(booleanSupplier: Supplier<Boolean>) = EnhancedBooleanSupplier { this.state and booleanSupplier.get() }
	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	override infix fun and(booleanSupplier: IEnhancedBooleanSupplier) = EnhancedBooleanSupplier { this.state and booleanSupplier.state }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	override infix fun or(booleanSupplier: Supplier<Boolean>) = EnhancedBooleanSupplier { this.state or booleanSupplier.get() }
	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	override infix fun or(booleanSupplier: IEnhancedBooleanSupplier) = EnhancedBooleanSupplier { this.state or booleanSupplier.state }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	override infix fun xor(booleanSupplier: Supplier<Boolean>) = EnhancedBooleanSupplier { this.state xor booleanSupplier.get() }
	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	override infix fun xor(booleanSupplier: IEnhancedBooleanSupplier) = EnhancedBooleanSupplier { this.state xor booleanSupplier.state }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that has the inverse of this, and keeps the debounce information
	 */
	override operator fun not() = EnhancedBooleanSupplier ({ !this.booleanSupplier.get() }, risingDebounce, fallingDebounce)

	//
	// Impl Feature:
	//
	@Suppress("LeakingThis")
	override val dependencies = DependencySet(this)
			.yields()

	init {
		@Suppress("LeakingThis")
		register()
	}

	/**
	 * if this automatically updates, by calling [invalidate] and [state]
	 */
	override var autoUpdates = true
	private fun autoUpdatePre() {
		if (autoUpdates) {
			state
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