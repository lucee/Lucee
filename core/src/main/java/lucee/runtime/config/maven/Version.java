package lucee.runtime.config.maven;
/*
 * Maven-compatible Version class.
 *
 * Key differences from OSGi Version:
 *  - minor, micro, and build may be null when not present in the original string
 *  - qualifier separator is '-' (Maven) not '.' (OSGi)
 *  - compareTo() uses Maven ordering rules
 *
 * Maven version grammar handled here:
 *   version   ::= major('.'minor('.'micro('.'build)?)?)?('-'qualifier)?
 *   major     ::= digit+
 *   minor     ::= digit+   (optional – null if absent)
 *   micro     ::= digit+   (optional – null if absent)
 *   build     ::= digit+   (optional – null if absent, e.g. "0.9.4.119-RC")
 *   qualifier ::= any non-empty string (SNAPSHOT, BETA, RC1, Final, …)
 */

import java.io.IOException;

import lucee.runtime.op.Caster;

public class Version implements Comparable<Version> {

	// ------------------------------------------------------------------ fields

	private final int major;
	private final Integer minor; // null when not specified in input
	private final Integer micro; // null when not specified in input
	private final Integer build; // null when not specified in input
	/** Empty string means "no qualifier", never null. */
	private final String qualifier;
	/** Canonical string built at construction time, used by toString(). */
	private final String original;

	private transient int hash; // lazy cache

	// --------------------------------------------------------- well-known constants

	/** The empty version "0.0.0". */
	public static final Version emptyVersion = new Version(0, 0, 0);

	// --------------------------------------------------------------- constructors

	/**
	 * Creates a version from numeric components; qualifier is set to "".
	 */
	public Version(int major, int minor, int micro) {
		this(major, minor, micro, null, null);
	}

	/**
	 * Internal constructor — all parsing goes through {@link #parseVersion(String)} or
	 * {@link #parseVersion(String, Version)}; this constructor only accepts already-validated
	 * components.
	 */
	private Version(int major, Integer minor, Integer micro, Integer build, String qualifier) {
		if (major < 0) throw new IllegalArgumentException("invalid version: negative major \"" + major + "\"");
		if (minor != null && minor < 0) throw new IllegalArgumentException("invalid version: negative minor \"" + minor + "\"");
		if (micro != null && micro < 0) throw new IllegalArgumentException("invalid version: negative micro \"" + micro + "\"");
		if (build != null && build < 0) throw new IllegalArgumentException("invalid version: negative build \"" + build + "\"");

		this.major = major;
		this.minor = minor;
		this.micro = micro;
		this.build = build;
		this.qualifier = (qualifier == null || qualifier.trim().isEmpty()) ? "" : qualifier.trim();
		this.original = buildString(major, minor, micro, build, this.qualifier);
	}

	// --------------------------------------------------------- static factories

	/** Static factory — returns {@link #emptyVersion} for null/empty input. */
	public static Version valueOf(String version) {
		if (version == null || version.trim().isEmpty()) return emptyVersion;
		return parseVersion(version, emptyVersion);
	}

	// ----------------------------------------------------------------- getters

	/** Returns the major component. Always present. */
	public int getMajor() {
		return major;
	}

	/**
	 * Returns the minor component exactly as parsed, or {@code null} if it was not present in the
	 * version string (e.g. {@code "2-BETA"}).
	 */
	public Integer getMinor() {
		return minor;
	}

	/**
	 * Returns the micro component exactly as parsed, or {@code null} if it was not present in the
	 * version string (e.g. {@code "2.5-BETA"}).
	 */
	public Integer getMicro() {
		return micro;
	}

	/**
	 * Returns the build component exactly as parsed, or {@code null} if it was not present in the
	 * version string (e.g. {@code "0.9.4-RC"} has no build, but {@code "0.9.4.119-RC"} has build 119).
	 */
	public Integer getBuild() {
		return build;
	}

	/** Returns the qualifier, or the empty string if there is none. */
	public String getQualifier() {
		return qualifier;
	}

	// ---------------------------------------------------------------- toString

	/**
	 * Returns the canonical string: "major", "major.minor", "major.minor.micro", or
	 * "major.minor.micro.build", each optionally followed by "-qualifier".
	 */
	@Override
	public String toString() {
		return original;
	}

	// --------------------------------------------------------------- hashCode / equals

	@Override
	public int hashCode() {
		int h = hash;
		if (h != 0) return h;
		h = 31 * 17;
		h = 31 * h + major;
		h = 31 * h + minor();
		h = 31 * h + micro();
		h = 31 * h + build();
		h = 31 * h + qualifier.hashCode();
		return hash = h;
	}

	/**
	 * Two versions are equal when major, effective minor, effective micro, effective build, and
	 * qualifier are all equal (absent components treated as 0).
	 * Note: "1.0.0" and "1.0.0.0" are considered equal by this contract.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Version)) return false;
		Version o = (Version) obj;
		return major == o.major && minor() == o.minor() && micro() == o.micro() && build() == o.build() && qualifier.equals(o.qualifier);
	}

	// --------------------------------------------------------------- compareTo

	/**
	 * Maven-style ordering:
	 * <ol>
	 * <li>Compare major, minor, micro, build numerically (absent treated as 0).</li>
	 * <li>A release (no qualifier) is newer than any pre-release.</li>
	 * <li>{@code SNAPSHOT} is always the oldest qualifier.</li>
	 * <li>Other qualifiers compared case-insensitively.</li>
	 * </ol>
	 */
	@Override
	public int compareTo(Version other) {
		if (other == this) return 0;

		int result = Integer.compare(major, other.major);
		if (result != 0) return result;

		result = Integer.compare(minor(), other.minor());
		if (result != 0) return result;

		result = Integer.compare(micro(), other.micro());
		if (result != 0) return result;

		result = Integer.compare(build(), other.build());
		if (result != 0) return result;

		return compareQualifiers(qualifier, other.qualifier);
	}

	public static int compare(Version v1, Version v2) {
		if (v1 == v2) return 0;
		if (v1 == null) return -1;
		if (v2 == null) return 1;
		return v1.compareTo(v2);
	}

	// --------------------------------------------------------------- parseVersion factories

	/**
	 * Lenient parse — returns {@code defaultValue} instead of throwing.
	 *
	 * Accepts both hyphen-separated qualifiers ("2.5.2-BETA") and dot-separated ones ("2.5.2.BETA"), as
	 * well as partial forms: "major", "major.minor", "major.minor.micro", "major.minor.micro.build".
	 *
	 * @param version the version string to parse; may be null/empty
	 * @param defaultValue returned when the string cannot be parsed
	 */
	public static Version parseVersion(String version, Version defaultValue) {
		if (version == null || version.trim().isEmpty()) return defaultValue;

		version = version.trim();
		String[] arr = version.split("\\.", -1);
		for (int i = 0; i < arr.length; i++)
			arr[i] = arr[i].trim();

		Integer major, minor, micro, build;
		String qualifier;

		switch (arr.length) {
		case 1: {
			String[] hp = arr[0].split("-", 2);
			major = Caster.toInteger(hp[0], null);
			minor = null;
			micro = null;
			build = null;
			qualifier = hp.length > 1 ? hp[1] : null;
			break;
		}
		case 2: {
			major = Caster.toInteger(arr[0], null);
			String[] hp = arr[1].split("-", 2);
			minor = Caster.toInteger(hp[0], null);
			micro = null;
			build = null;
			qualifier = hp.length > 1 ? hp[1] : null;
			break;
		}
		case 3: {
			major = Caster.toInteger(arr[0], null);
			minor = Caster.toInteger(arr[1], null);
			String[] hp = arr[2].split("-", 2);
			micro = Caster.toInteger(hp[0], null);
			build = null;
			qualifier = hp.length > 1 ? hp[1] : null;
			break;
		}
		default: {
			// 4 dot-parts: "major.minor.micro.build[-qualifier]" — 5+ segments are not supported and will return defaultValue
			major = Caster.toInteger(arr[0], null);
			minor = Caster.toInteger(arr[1], null);
			micro = Caster.toInteger(arr[2], null);
			String[] hp = arr[3].split("-", 2);
			build = Caster.toInteger(hp[0], null);
			qualifier = hp.length > 1 ? hp[1] : null;
			break;
		}
		}

		if (major == null || (arr.length >= 2 && minor == null) || (arr.length >= 3 && micro == null) || (arr.length >= 4 && build == null))
			return defaultValue;

		return new Version(major, minor, micro, build, qualifier);
	}

	/**
	 * Strict parse — throws {@code IOException} on failure.
	 *
	 * @throws IOException if {@code version} cannot be parsed
	 */
	public static Version parseVersion(String version) throws IOException {
		Version v = parseVersion(version, null);
		if (v != null) return v;
		throw new IOException("Given version [" + version + "] is invalid, a valid version follows the pattern <major>[.<minor>[.<micro>[.<build>]]][-<qualifier>]");
	}

	// ----------------------------------------------------------------- helpers

	/** Returns minor as int, treating absent (null) as 0. */
	private int minor() {
		return minor == null ? 0 : minor;
	}

	/** Returns micro as int, treating absent (null) as 0. */
	private int micro() {
		return micro == null ? 0 : micro;
	}

	/** Returns build as int, treating absent (null) as 0. */
	private int build() {
		return build == null ? 0 : build;
	}

	/** Builds the canonical string from components. */
	private static String buildString(int major, Integer minor, Integer micro, Integer build, String qualifier) {
		StringBuilder sb = new StringBuilder(24);
		sb.append(major);
		if (minor != null) {
			sb.append('.').append(minor);
			if (micro != null) {
				sb.append('.').append(micro);
				if (build != null) sb.append('.').append(build);
			}
		}
		if (qualifier != null && !qualifier.isEmpty()) sb.append('-').append(qualifier);
		return sb.toString();
	}

	/**
	 * Maven qualifier ordering: - empty (release) beats any qualifier - SNAPSHOT is always the lowest
	 * qualifier - everything else compared case-insensitively
	 */
	private static int compareQualifiers(String q1, String q2) {
		boolean e1 = q1.isEmpty(), e2 = q2.isEmpty();
		if (e1 && e2) return 0;
		if (e1) return 1;
		if (e2) return -1;
		boolean s1 = q1.equalsIgnoreCase("SNAPSHOT");
		boolean s2 = q2.equalsIgnoreCase("SNAPSHOT");
		if (s1 && s2) return 0;
		if (s1) return -1;
		if (s2) return 1;
		return q1.compareToIgnoreCase(q2);
	}
}
