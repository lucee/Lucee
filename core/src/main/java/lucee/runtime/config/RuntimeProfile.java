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

	static {
		ALLOW_PRECISE_MATH = Caster.toBooleanValue( SystemUtil.getSystemPropOrEnvVar( "lucee.allow.precise.math", null ), true );
	}

	// Private constructor to prevent instantiation
	private RuntimeProfile() {
	}
}
