package lucee.runtime.config;

import lucee.commons.io.SystemUtil;
import lucee.runtime.op.Caster;

/**
 * Runtime configuration profile using static final boolean flags for JIT optimization.
 *
 * This class provides compile-time constant flags that enable JIT dead code elimination
 * when features are disabled globally. The JVM HotSpot compiler can optimize away entire
 * code branches when these flags are false, resulting in zero runtime overhead.
 *
 * All flags are set once at startup from environment variables or system properties
 * and cannot be changed at runtime, allowing maximum optimization.
 */
public final class RuntimeProfile {

	/**
	 * Controls whether applications are allowed to enable precise math.
	 *
	 * When set to false, all precise math code paths can be optimized away by the JIT compiler,
	 * providing zero overhead for production systems that don't need BigDecimal precision.
	 *
	 * Environment Variable: LUCEE_ALLOW_PRECISE_MATH
	 * System Property: lucee.allow.precise.math
	 * Default: true (applications can enable precise math)
	 *
	 * When false:
	 * - Applications cannot enable precise math (throws error if attempted)
	 * - All precise math checks become compile-time dead code
	 * - Zero runtime overhead for the feature
	 */
	public static final boolean ALLOW_PRECISE_MATH;

	/**
	 * Controls whether applications are allowed to enable full null support.
	 *
	 * When set to false, all full null support code paths can be optimized away by the JIT compiler,
	 * providing zero overhead for production systems that don't use full null support.
	 *
	 * Environment Variable: LUCEE_ALLOW_FULL_NULL_SUPPORT
	 * System Property: lucee.allow.full.null.support
	 * Default: true (applications can enable full null support)
	 *
	 * When false:
	 * - Applications cannot enable full null support
	 * - All null support checks (49+ call sites across 18 files) become compile-time dead code
	 * - Zero runtime overhead for the feature
	 */
	public static final boolean ALLOW_FULL_NULL_SUPPORT;

	static {
		ALLOW_PRECISE_MATH = Caster.toBooleanValue( SystemUtil.getSystemPropOrEnvVar( "lucee.allow.precise.math", null ), true );
		ALLOW_FULL_NULL_SUPPORT = Caster.toBooleanValue( SystemUtil.getSystemPropOrEnvVar( "lucee.allow.full.null.support", null ), true );
	}

	// Private constructor to prevent instantiation
	private RuntimeProfile() {
	}
}
