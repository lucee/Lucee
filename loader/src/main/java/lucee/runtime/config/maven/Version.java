package lucee.runtime.config.maven;

import java.io.Serializable;

import lucee.StableInMajor;

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
 * <p>
 * The implementation is {@code lucee.loader.util.VersionImpl}, use {@code CFMLEngineFactorySupport.toVersion}
 * to get an instance.
 */
@StableInMajor
public interface Version extends Comparable<Version>, Serializable {

	public static final int SNAPSHOT = 1;
	public static final int RELEASE = 2;

	public int getMajor();

	public int getMinor();

	public int getMicro();

	/** the fourth number, for example the build of Lucee (9.0.0.<b>12</b>-ALPHA) */
	public int getBuild();

	/** the qualifier after the '-', for example "ALPHA", an empty string when there is none */
	public String getQualifier();

	/**
	 * Returns the string this version was parsed from.
	 */
	@Override
	public String toString();

	public boolean is(int type);

	public String cycle();

	/**
	 * Ordering:
	 * <ol>
	 * <li>major, minor, micro and build are compared as numbers (a part that is not there is 0)</li>
	 * <li>a release (no qualifier) is newer than any version with a qualifier</li>
	 * <li>a qualifier ending with SNAPSHOT is older than any other qualifier</li>
	 * <li>other qualifiers are compared case-insensitively (ALPHA, BETA, RC)</li>
	 * </ol>
	 */
	@Override
	public int compareTo(Version other);
}
