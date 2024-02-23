package dev.frozenmilk.dairy.core.dependencyresolution.dependencies

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import dev.frozenmilk.dairy.core.Feature
import dev.frozenmilk.dairy.core.dependencyresolution.FeatureDependencyResolutionFailureException
import java.util.function.Consumer

sealed interface Dependency<OUTPUT, ARGS> {
	/**
	 * the feature which this resolves to
	 */
	val feature: Feature

	/**
	 * returns <true, resolving arguments> if this resolves against the found arguments
	 * returns <false, empty()> if this fails to resolve against the found arguments
	 */
	fun resolves(args: ARGS): Pair<Boolean, OUTPUT>

	/**
	 * throws an error if the dependency doesn't resolve that contains some helpful diagnostic information
	 * @see[FeatureDependencyResolutionFailureException]
	 */
	@Throws(FeatureDependencyResolutionFailureException::class)
	fun resolvesOrError(args: ARGS): Pair<Boolean, OUTPUT> {
		val resolution = resolves(args)
		if (!resolution.first) throw FeatureDependencyResolutionFailureException(feature, dependencyResolutionFailureMessage, failures)
		return resolution
	}

	val failures: Collection<String>
	val dependencyResolutionFailureMessage: String

	/**
	 * validates arguments, can be expensive to run
	 */
	fun validateContents()

	fun bindOutput(output: Consumer<OUTPUT>) {
		outputRef = output
	}

	var outputRef: Consumer<OUTPUT>?
	fun acceptResolutionOutput(output: OUTPUT) {
		outputRef?.accept(output)
	}
}

/**
 * ensures one of the arguments are attached and processed first before this, otherwise doesn't attach it, returns the first one found
 */
class DependsOnOneOf(override val feature: Feature, private vararg val features: Class<out Feature>) : Dependency<Feature, Collection<Feature>> {
	override fun resolves(args: Collection<Feature>): Pair<Boolean, Feature> {
		args.forEach {
			if (it::class.java in features) return true to it
		}
		return false to FeatureFalse // returns a dummy feature, which shouldn't reach the end user as dependency resolution failed
	}

	override val failures: Collection<String> = features.map { it.simpleName }
	override val dependencyResolutionFailureMessage = "found features did not include at least one of the following types"
	override fun validateContents() {
		if (feature::class.java in features) throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: it is self dependant/exclusive")
	}

	override var outputRef: Consumer<Feature>? = null
}

abstract class FeatureDependency(override val feature: Feature, protected vararg val features: Class<out Feature>) : Dependency<Collection<Feature>, Collection<Feature>> {
	final override fun validateContents() {
		if (feature::class.java in features) throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: it is self dependant/exclusive")
	}

	override var outputRef: Consumer<Collection<Feature>>? = null
}

abstract class FlagDependency(override val feature: Feature, protected vararg val flags: Class<out Annotation>) : Dependency<Collection<Annotation>, Collection<Annotation>> {
	final override fun validateContents() {
		if (
				TeleOp::class.java in flags ||
				Autonomous::class.java in flags ||
				Disabled::class.java in flags
		)
			throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: annotations that are used as part of the base sdk are illegal flag dependency arguments")
	}

	override var outputRef: Consumer<Collection<Annotation>>? = null
}

/**
 * resolves if the flags on the OpMode contain exactly one of these Flags
 */
class IncludesExactlyOneOf(override val feature: Feature, vararg val flags: Class<out Annotation>) : Dependency<Annotation, Collection<Annotation>> {
	override fun resolves(args: Collection<Annotation>): Pair<Boolean, Annotation> {
		failures.clear()
		var result: Annotation? = null
		var outcome = false
		args.forEach {
			if(it.annotationClass.java in flags) {
				if(!outcome) {
					outcome = true
					result = it
				}
				else {
					result = null
					failures.add(it.annotationClass.java.simpleName)
				}
			}
		}
		return outcome to (result ?: False()) // returns a dummy annotation if result was never found
	}

	override val failures: MutableSet<String> = mutableSetOf();
	override val dependencyResolutionFailureMessage: String
		get() {
			if(failures.isEmpty()) {
				failures.addAll(flags.map { it.simpleName })
				return "found flags did not include exactly one of the following"
			}
			return "found excess flags"
		}

	override fun validateContents() {
		if (
				TeleOp::class.java in flags ||
				Autonomous::class.java in flags ||
				Disabled::class.java in flags
		)
			throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: annotations that are used as part of the base sdk are illegal flag dependency arguments")
	}

	override var outputRef: Consumer<Annotation>? = null
}

/**
 * causes a dependency to try to mount after others
 */
class Yields(override val feature: Feature) : Dependency<Any?, Boolean> {
	override fun resolves(args: Boolean): Pair<Boolean, Any?> = args to null

	override val failures: Collection<String> = emptyList();
	override val dependencyResolutionFailureMessage: String = "failed to yield";
	override fun validateContents() {}

	override var outputRef: Consumer<Any?>? = null // never set to anything else
}

/**
 * Resolves if this successfully yields to at least one of all [features], returns all features which are of the specified classes
 */
class YieldsTo(override val feature: Feature, private vararg val features: Class<out Feature>) : Dependency<Collection<Feature>, Pair<Boolean, Collection<Feature>>> {
	override fun resolves(args: Pair<Boolean, Collection<Feature>>): Pair<Boolean, Collection<Feature>> {
		if (args.first) {
			val result = mutableSetOf<Feature>()
			args.second.forEach {
				if (it::class.java in features) result.add(it)
			}
			val classResult = result.map { it::class.java }.toSet()
			var output = true
			features.forEach {
				val found = it in classResult
				output = output and found
				if (!found) failures.add(it.simpleName)
			}
			return output to result
		}
		return false to emptySet()
	}

	override val failures: MutableList<String> = mutableListOf()

	override val dependencyResolutionFailureMessage: String = "failed to yield to"
	override fun validateContents() {
		if (feature::class.java in features) throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: it is self dependant/exclusive")
	}

	override var outputRef: Consumer<Collection<Feature>>? = null
}

class DependsDirectlyOn(override val feature: Feature, private vararg  val features: Feature) : Dependency<Collection<Feature>, Collection<Feature>> {
	override fun resolves(args: Collection<Feature>): Pair<Boolean, Collection<Feature>> {
		var resolves = true
		features.forEach {
			val found = it in args
			resolves = resolves and found
			if (!found) failures.add("unable to find feature of type: ${feature.javaClass.simpleName}")
		}
		// args is only used if we resolved
		return resolves to args
	}

	override val failures: MutableList<String> = mutableListOf()

	override val dependencyResolutionFailureMessage: String = "not all required features were attached"

	override fun validateContents() {
		if (feature in features) throw IllegalArgumentException("${feature.javaClass.simpleName} has an illegal dependency set: it is self dependant/exclusive")
	}

	override var outputRef: Consumer<Collection<Feature>>? = null
}