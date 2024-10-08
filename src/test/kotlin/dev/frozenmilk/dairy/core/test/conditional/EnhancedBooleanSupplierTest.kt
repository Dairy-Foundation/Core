package dev.frozenmilk.dairy.core.test.conditional

import dev.frozenmilk.dairy.core.util.supplier.logical.EnhancedBooleanSupplier
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class EnhancedBooleanSupplierTest {
	private var state: Boolean = false
	private val enhancedBooleanSupplier = EnhancedBooleanSupplier { state }

	@Before
	fun before() {
		state = false
		enhancedBooleanSupplier.state
		enhancedBooleanSupplier.invalidate()
	}
	@Test
	fun testStateChanges() {
		testState(
				new = false,
				expected = false,
		)
		testState(
				new = true,
				expected = true,
		)
		testState(
				new = true,
				expected = true,
		)
		testState(
				new = false,
				expected = false,
		)
		testState(
				new = false,
				expected = false,
		)
		testState(
				new = true,
				expected = true,
		)
	}
	@Test
	fun testEdgeDetectionChanges() {
		testRisingEdge(
				new = false,
				expected = false,
		)
		testRisingEdge(
				new = true,
				expected = true,
		)
		testRisingEdge(
				new = true,
				expected = false,
		)
		testFallingEdge(
				new = false,
				expected = true,
		)
		testFallingEdge(
				new = false,
				expected = false,
		)
		testRisingEdge(
				new = true,
				expected = true,
		)
		testFallingEdge(
				new = false,
				expected = true
		)
	}
	fun testFallingEdge(new: Boolean, expected: Boolean) {
		state = new
		val res = enhancedBooleanSupplier.onFalse
		Assert.assertEquals(expected, res)
		enhancedBooleanSupplier.invalidate()
	}
	fun testRisingEdge(new: Boolean, expected: Boolean) {
		state = new
		val res = enhancedBooleanSupplier.onTrue
		Assert.assertEquals(expected, res)
		enhancedBooleanSupplier.invalidate()
	}
	fun testState(new: Boolean, expected: Boolean) {
		state = new
		val res = enhancedBooleanSupplier.state
		Assert.assertEquals(expected, res)
		enhancedBooleanSupplier.invalidate()
	}
}