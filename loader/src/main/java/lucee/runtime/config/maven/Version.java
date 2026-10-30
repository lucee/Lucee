package lucee.runtime.config.maven;

import java.io.IOException;
import java.io.Serializable;

/**
 * The version type of Lucee, used for the version of Lucee, of extensions, of libraries and of
 * bundles. It is Maven compatible, but can also be parsed with the stricter rules of OSGi.
 * <p>
 * Differences to the OSGi Version:
 * <ul>
 * <li>minor, micro and build are optional</li>
 * <li>the qualifier is separated with '-' (Maven), not with '.' (OSGi)</li>
 * <li>compareTo() uses Maven like ordering rules, see {@link #compareTo(Version)}</li>
 * </ul>
 * Grammar:
 *
 * <pre>
 *   version   ::= major('.'minor('.'micro('.'build)?)?)?('-'qualifier)?
 *   major     ::= digit+
 *   minor     ::= digit+   (optional, 0 if absent)
 *   micro     ::= digit+   (optional, 0 if absent)
 *   build     ::= digit+   (optional, 0 if absent, e.g. "0.9.4.119-RC")
 *   qualifier ::= any non-empty string (SNAPSHOT, BETA, RC1, Final, ...)
 * </pre>
 */
public class Version implements Comparable<Version>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final int SNAPSHOT = 1;
	public static final int RELEASE = 2;

	// ------------------------------------------------------------------ fields

	private final Integer major;
	private final Integer minor;
	private final Integer micro;
	private final Integer build;
	private final String appendix;

	private final String original;

	/** false when a part that is there is not a number (a lenient parse accepts that, a strict parse does not) */
	private final boolean numeric;

	/** The empty version "0.0.0". */
	public static final Version emptyVersion = new Version(null, null, null, null, null, "", true);

	private Version(Integer major, Integer minor, Integer micro, Integer build, String appendix, String original, boolean numeric) {
		this.major = major;
		this.minor = minor;
		this.micro = micro;
		this.build = build;
		this.appendix = appendix;
		this.original = original;
		this.numeric = numeric;
	}

	// ------------------------------------------------------------------ access

	public int getMajor() {
		return major == null ? 0 : major;
	}

	public int getMinor() {
		return minor == null ? 0 : minor;
	}

	public int getMicro() {
		return micro == null ? 0 : micro;
	}

	/** the fourth number, for example the build of Lucee (9.0.0.<b>12</b>-ALPHA) */
	public int getBuild() {
		return build == null ? 0 : build;
	}

	/** the qualifier after the '-', for example "ALPHA", an empty string when there is none */
	public String getQualifier() {
		return appendix == null ? "" : appendix;
	}

	/**
	 * Returns the string this version was parsed from.
	 */
	@Override
	public String toString() {
		return original;
	}

	public boolean is(int type) {
		if (appendix != null && appendix.endsWith("SNAPSHOT")) return type == SNAPSHOT;
		return type == RELEASE;
	}

	public String cycle() {
		return major + ":" + minor + ":" + micro;
	}

	// --------------------------------------------------------------- hashCode / equals

	@Override
	public int hashCode() {
		if (!numeric) return original.hashCode();
		return (((getMajor() * 31 + getMinor()) * 31 + getMicro()) * 31 + getBuild()) * 31 + getQualifier().toLowerCase().hashCode();
	}

	/**
	 * Two versions are equal when they are equal for compareTo(), "1.0" and "1.0.0.0" are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Version)) return false;
		return compareTo((Version) obj) == 0;
	}

	// --------------------------------------------------------------- compareTo

	/**
	 * Ordering:
	 * <ol>
	 * <li>major, minor, micro and build are compared as numbers (a part that is not there is 0)</li>
	 * <li>a release (no qualifier) is newer than any version with a qualifier</li>
	 * <li>a qualifier ending with SNAPSHOT is older than any other qualifier</li>
	 * <li>other qualifiers are compared case-insensitively (ALPHA, BETA, RC)</li>
	 * </ol>
	 * When a part is not a number (only possible with a lenient parse), the versions are compared as
	 * strings.
	 */
	@Override
	public int compareTo(Version other) {
		if (other == this) return 0;

		if (!numeric || !other.numeric) return original.compareTo(other.original);

		int result = Integer.compare(getMajor(), other.getMajor());
		if (result != 0) return result;
		result = Integer.compare(getMinor(), other.getMinor());
		if (result != 0) return result;
		result = Integer.compare(getMicro(), other.getMicro());
		if (result != 0) return result;
		result = Integer.compare(getBuild(), other.getBuild());
		if (result != 0) return result;

		String a = getQualifier();
		String b = other.getQualifier();
		int ra = rank(a);
		int rb = rank(b);
		if (ra != rb) return Integer.compare(ra, rb);
		return a.compareToIgnoreCase(b);
	}

	private static int rank(String qualifier) {
		if (qualifier.isEmpty()) return 2; // release
		if (qualifier.toUpperCase().endsWith("SNAPSHOT")) return 0;
		return 1;
	}

	public static int compare(Version v1, Version v2) {
		if (v1 == v2) return 0;
		if (v1 == null) return -1;
		if (v2 == null) return 1;
		return v1.compareTo(v2);
	}

	// --------------------------------------------------------------- parsing

	/** Static factory, returns {@link #emptyVersion} for null/empty input. */
	public static Version valueOf(String version) {
		if (version == null || version.trim().isEmpty()) return emptyVersion;
		return parseVersion(version, emptyVersion);
	}

	/**
	 * Lenient parse, returns {@code defaultValue} instead of throwing.
	 *
	 * Accepts qualifiers with a hyphen ("2.5.2-BETA") and partial forms: "major", "major.minor",
	 * "major.minor.micro", "major.minor.micro.build".
	 *
	 * @param version the version string to parse, may be null/empty
	 * @param defaultValue returned when the string cannot be parsed
	 */
	public static Version parseVersion(String version, Version defaultValue) {
		return parse(version, false, defaultValue);
	}

	/**
	 * Lenient parse, throws an {@code IOException} on failure.
	 */
	public static Version parseVersion(String version) throws IOException {
		return parse(version, false);
	}

	/**
	 * Parse a version.
	 *
	 * @param version the version string to parse, may be null/empty
	 * @param strict false: the rules of Maven, a part that is not a number is accepted. true: the rules
	 *            of OSGi, major.minor.micro[.build][-qualifier], every part that is there must be a
	 *            number, so the result can be used for a bundle
	 * @param defaultValue returned when the string cannot be parsed
	 */
	public static Version parse(String version, boolean strict, Version defaultValue) {
		if (version == null || version.trim().isEmpty()) return defaultValue;

		version = version.trim();
		String[] arr = version.split("\\.", -1);
		for (int i = 0; i < arr.length; i++)
			arr[i] = arr[i].trim();

		Integer major, minor = null, micro = null, build = null;
		String appendix;
		boolean numeric;

		switch (arr.length) {
		case 1: {
			String[] hp = arr[0].split("-", 2);
			major = toInteger(hp[0]);
			appendix = hp.length > 1 ? hp[1] : null;
			numeric = major != null;
			break;
		}
		case 2: {
			major = toInteger(arr[0]);
			String[] hp = arr[1].split("-", 2);
			minor = toInteger(hp[0]);
			appendix = hp.length > 1 ? hp[1] : null;
			numeric = major != null && minor != null;
			break;
		}
		case 3: {
			major = toInteger(arr[0]);
			minor = toInteger(arr[1]);
			String[] hp = arr[2].split("-", 2);
			micro = toInteger(hp[0]);
			appendix = hp.length > 1 ? hp[1] : null;
			numeric = major != null && minor != null && micro != null;
			break;
		}
		default: {
			major = toInteger(arr[0]);
			minor = toInteger(arr[1]);
			micro = toInteger(arr[2]);
			String[] hp = arr[3].split("-", 2);
			build = toInteger(hp[0]);
			appendix = hp.length > 1 ? hp[1] : null;
			numeric = major != null && minor != null && micro != null && build != null;
			break;
		}
		}
		if (strict && !numeric) return defaultValue;
		if (appendix != null && appendix.trim().isEmpty()) appendix = null;
		return new Version(major, minor, micro, build, appendix, version, numeric);
	}

	/**
	 * Parse a version, throws an {@code IOException} on failure.
	 */
	public static Version parse(String version, boolean strict) throws IOException {
		Version v = parse(version, strict, null);
		if (v != null) return v;
		throw new IOException("Given version [" + version + "] is invalid, a valid version follows the pattern <major>[.<minor>[.<micro>[.<build>]]][-<qualifier>]"
				+ (strict ? ", every number must be a number" : ""));
	}

	private static Integer toInteger(String str) {
		if (str == null) return null;
		str = str.trim();
		if (str.isEmpty() || str.length() > 9) return null;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) < '0' || str.charAt(i) > '9') return null;
		}
		return Integer.valueOf(str);
	}
}
