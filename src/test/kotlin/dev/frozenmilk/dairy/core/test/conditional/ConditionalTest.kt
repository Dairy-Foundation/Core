package dev.frozenmilk.dairy.core.test.conditional

import dev.frozenmilk.dairy.core.util.supplier.numeric.EnhancedDoubleSupplier
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ConditionalTest {
	private var testState = 0.0
	private lateinit var enhancedDoubleSupplier: EnhancedDoubleSupplier
	@Before
	fun setup() {
		testState = 0.0
		enhancedDoubleSupplier = EnhancedDoubleSupplier({ testState })
	}
	@Test
	fun lessThan1() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.lessThan(10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun lessThan2() {
		testState = 20.0
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.lessThan(10.0)
			.bind()
		Assert.assertFalse(binding.state)
	}
	@Test
	fun greaterThan1() {
		testState = 20.0
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.greaterThan(10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun greaterThan2() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.greaterThan(10.0)
			.bind()
		Assert.assertFalse(binding.state)
	}
	@Test
	fun lessThanEqualTo1() {
		testState = 10.0
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.lessThanEqualTo(10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun lessThanEqualTo2() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.lessThanEqualTo(10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun lessThanEqualTo3() {
		testState = 20.0
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.lessThanEqualTo(10.0)
			.bind()
		Assert.assertFalse(binding.state)
	}
	@Test
	fun greaterThanEqualTo1() {
		testState = 10.0
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.greaterThanEqualTo(10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun greaterThanEqualTo2() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.greaterThanEqualTo(10.0)
			.bind()
		Assert.assertFalse(binding.state)
	}
	@Test
	fun greaterThanEqualTo3() {
		testState = 20.0
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.greaterThanEqualTo(10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun range1() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.lessThan(10.0)
			.greaterThan(-10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun range2() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.greaterThan(-10.0)
			.lessThan(10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun range3() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.greaterThan(10.0)
			.lessThan(-10.0)
			.bind()
		binding.autoUpdates
		Assert.assertFalse(binding.state)
	}
	@Test
	fun range4() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.lessThan(-10.0)
			.greaterThan(10.0)
			.bind()
		Assert.assertFalse(binding.state)
	}
	@Test
	fun range5() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.lessThan(-10.0)
			.greaterThan(10.0)
			.lessThan(1.0)
			.greaterThan(-1.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun doubled1() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.lessThan(-10.0)
			.lessThan(10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun doubled2() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.lessThan(10.0)
			.lessThan(-10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun doubled3() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.greaterThan(-10.0)
			.greaterThan(10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun doubled4() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.greaterThan(10.0)
			.greaterThan(-10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun doubled5() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.lessThanEqualTo(-10.0)
			.lessThanEqualTo(10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun doubled6() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.lessThanEqualTo(10.0)
			.lessThanEqualTo(-10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun doubled7() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.greaterThanEqualTo(-10.0)
			.greaterThanEqualTo(10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
	@Test
	fun doubled8() {
		val binding = enhancedDoubleSupplier.conditionalBindState()
			.greaterThanEqualTo(10.0)
			.greaterThanEqualTo(-10.0)
			.bind()
		Assert.assertTrue(binding.state)
	}
}
