package dev.frozenmilk.dairy.core.util.supplier.logical

import dev.frozenmilk.dairy.core.dependency.Dependency
import dev.frozenmilk.dairy.core.dependency.lazy.Yielding
import dev.frozenmilk.dairy.core.wrapper.Wrapper
import java.util.function.BooleanSupplier

/**
 * [deregister]s at the end of an OpMode
 */
@Suppress("INAPPLICABLE_JVM_NAME")
class EnhancedBooleanSupplier private constructor(private val booleanSupplier: BooleanSupplier, private val risingDebounce: Long, private val fallingDebounce: Long) : IEnhancedBooleanSupplier<EnhancedBooleanSupplier> {
	constructor(booleanSupplier: BooleanSupplier, risingDebounce: Double, fallingDebounce: Double) : this(booleanSupplier, (risingDebounce * 1E9).toLong(), (fallingDebounce * 1E9).toLong())
	constructor(booleanSupplier: BooleanSupplier) : this(booleanSupplier, 0, 0)
	private var previous = booleanSupplier.asBoolean
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
		if(!current && booleanSupplier.asBoolean){
			if(time - timeMarker >= risingDebounce) {
				current = true
				_toggleTrue = !_toggleTrue
				timeMarker = time
			}
		}
		else if (current && !booleanSupplier.asBoolean) {
			if (time - timeMarker >= fallingDebounce) {
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
	override infix fun and(booleanSupplier: BooleanSupplier) = EnhancedBooleanSupplier { this.state && booleanSupplier.asBoolean }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	override infix fun and(booleanSupplier: IEnhancedBooleanSupplier<*>) = EnhancedBooleanSupplier { this.state && booleanSupplier.state }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	override fun or(booleanSupplier: BooleanSupplier) = EnhancedBooleanSupplier { this.state || booleanSupplier.asBoolean }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	override infix fun or(booleanSupplier: IEnhancedBooleanSupplier<*>) = EnhancedBooleanSupplier { this.state || booleanSupplier.state }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	override infix fun xor(booleanSupplier: BooleanSupplier) = EnhancedBooleanSupplier { this.state xor booleanSupplier.asBoolean }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that combines the two conditions
	 */
	override infix fun xor(booleanSupplier: IEnhancedBooleanSupplier<*>) = EnhancedBooleanSupplier { this.state xor booleanSupplier.state }

	/**
	 * non-mutating
	 *
	 * @return a new EnhancedBooleanSupplier that has the inverse of this, and keeps the debounce information
	 */
	override operator fun not() = EnhancedBooleanSupplier({ !this.state }, risingDebounce, fallingDebounce)

	//
	// Impl Feature:
	//
	override var dependency: Dependency<*> = Yielding

	init {
		register()
	}

	/**
	 * if this automatically updates, by calling [invalidate] and [state]
	 */
	override var autoUpdates = true
	private fun autoUpdatePost() {
		if (autoUpdates) {
			invalidate()
			state
		}
	}

	override fun postUserInitHook(opMode: Wrapper) = autoUpdatePost()
	override fun postUserInitLoopHook(opMode: Wrapper) = autoUpdatePost()
	override fun postUserStartHook(opMode: Wrapper) = autoUpdatePost()
	override fun postUserLoopHook(opMode: Wrapper) = autoUpdatePost()
	override fun cleanup(opMode: Wrapper) {
		deregister()
	}
}