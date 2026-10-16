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

import lucee.aprint;
import lucee.runtime.op.Caster;

public class Version implements Comparable<Version> {

	public static final int SNAPSHOT = 1;
	public static final int RELEASE = 2;

	// ------------------------------------------------------------------ fields

	private final Integer major;
	private final Integer minor;
	private final Integer micro;
	private final Integer build;
	private final String appendix;

	private final String original;

	private transient int hash; // lazy cache

	// --------------------------------------------------------- well-known constants

	/** The empty version "0.0.0". */
	public static final Version emptyVersion = new Version(null, null, null, null, null, "");

	/**
	 * Internal constructor — all parsing goes through {@link #parseVersion(String)} or
	 * {@link #parseVersion(String, Version)}; this constructor only accepts already-validated
	 * components.
	 */
	private Version(Integer major, Integer minor, Integer micro, Integer build, String appendix, String original) {

		this.major = major;
		this.minor = minor;
		this.micro = micro;
		this.build = build;
		this.appendix = appendix;
		this.original = original;
	}

	// --------------------------------------------------------- static factories

	/** Static factory — returns {@link #emptyVersion} for null/empty input. */
	public static Version valueOf(String version) {
		if (version == null || version.trim().isEmpty()) return emptyVersion;
		return parseVersion(version, emptyVersion);
	}

	/**
	 * Returns the canonical string: "major", "major.minor", "major.minor.micro", or
	 * "major.minor.micro.build", each optionally followed by "-qualifier".
	 */
	@Override
	public String toString() {
		return original;
	}

	public boolean is(int type) {
		if (appendix != null && appendix.endsWith("SNAPSHOT")) return type == SNAPSHOT;
		return type == RELEASE;
	}

	// --------------------------------------------------------------- hashCode / equals

	@Override
	public int hashCode() {
		return original.hashCode();
	}

	/**
	 * Two versions are equal when major, effective minor, effective micro, effective build, and
	 * qualifier are all equal (absent components treated as 0). Note: "1.0.0" and "1.0.0.0" are
	 * considered equal by this contract.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Version)) return false;
		Version o = (Version) obj;
		return original.equals(o.original);
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

		// we only check if we have integer on both
		if (major != null && other.major != null) {
			int result = Integer.compare(major, other.major);
			if (result != 0) return result;

			// we only check if we also had major
			if (minor != null && other.minor != null) {
				result = Integer.compare(minor, other.minor);
				if (result != 0) return result;

				// we only check if we also had minor
				if (micro != null && other.micro != null) {
					result = Integer.compare(micro, other.micro);
					if (result != 0) return result;

					// we only check if we also had minor
					if (build != null && other.build != null) {
						result = Integer.compare(build, other.build);
						if (result != 0) return result;

						// we only check if we also had build
						return (appendix + "").compareTo(other.appendix + "");
					}
				}
			}
		}
		return original.compareTo(other.original);
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

		Integer major, minor = null, micro = null, build = null;
		String appendix;

		switch (arr.length) {
		case 1: {
			String[] hp = arr[0].split("-", 2);
			major = Caster.toInteger(hp[0], null);
			appendix = hp.length > 1 ? hp[1] : null;
			break;
		}
		case 2: {
			major = Caster.toInteger(arr[0], null);
			String[] hp = arr[1].split("-", 2);
			minor = Caster.toInteger(hp[0], null);
			appendix = hp.length > 1 ? hp[1] : null;
			break;
		}
		case 3: {
			major = Caster.toInteger(arr[0], null);
			minor = Caster.toInteger(arr[1], null);
			String[] hp = arr[2].split("-", 2);
			micro = Caster.toInteger(hp[0], null);
			appendix = hp.length > 1 ? hp[1] : null;
			break;
		}
		default: {
			major = Caster.toInteger(arr[0], null);
			minor = Caster.toInteger(arr[1], null);
			micro = Caster.toInteger(arr[2], null);
			String[] hp = arr[3].split("-", 2);
			build = Caster.toInteger(hp[0], null);
			appendix = hp.length > 1 ? hp[1] : null;
			break;
		}
		}

		return new Version(major, minor, micro, build, appendix, version);
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

	/** Builds the canonical string from components. */
	private static String buildString(int major, Integer minor, Integer micro, String qualifier) {
		StringBuilder sb = new StringBuilder(24);
		sb.append(major);
		if (minor != null) {
			sb.append('.').append(minor);
			if (micro != null) {
				sb.append('.').append(micro);
				if (qualifier != null) sb.append('.').append(qualifier);
			}
		}
		return sb.toString();
	}

	public static void dump(String version) throws IOException {
		Version v = parseVersion(version);
		aprint.e("---- " + version + " ----");
		aprint.e("str: " + v.toString());
		aprint.e("original: " + v.original);
		aprint.e("major:" + v.major);
		aprint.e("minor:" + v.minor);
		aprint.e("micro:" + v.micro);
		aprint.e("build:" + v.build);
		aprint.e("appendix:" + v.appendix);
		aprint.e("is-rel:" + v.is(RELEASE));
		aprint.e("is-snap:" + v.is(SNAPSHOT));
		aprint.e("");
	}

	public String cycle() {
		return major + ":" + minor + ":" + micro;
	}

}
