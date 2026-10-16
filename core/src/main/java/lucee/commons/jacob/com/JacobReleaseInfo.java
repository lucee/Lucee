package lucee.commons.jacob.com;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * An interface to the version properties file. This code was removed from JacobObject because it
 * doesn't belong there.
 * 
 */
public class JacobReleaseInfo {

	/**
	 * holds the build version as retrieved from the version properties file that exists in the JAR.
	 * This can be retrieved by calling the static method getBuildVersion()
	 * 
	 * @see #getBuildVersion()
	 */
	private static String buildVersion = "";
	/**
	 * holds the build date as retrieved from the version properties file that exists in the JAR This
	 * can be retrieved by calling the static method getBuildDate()
	 * 
	 * @see #getBuildDate()
	 */
	private static String buildDate = "";
	/** the name of the jacob version properties file */
	private static final String PROPERTY_FILE_NAME = "META-INF/JacobVersion.properties";

	/**
	 * Loads version information from PROPERTY_FILE_NAME that was built as part of this release.
	 * 
	 * @throws IllegalStateException when it can't find the version properties file
	 */
	private static void loadVersionProperties() {
		Properties versionProps = new Properties();
		// can't use system class loader cause won't work in JavaWebStart
		InputStream stream = JacobReleaseInfo.class.getClassLoader().getResourceAsStream(PROPERTY_FILE_NAME);
		// This should never happen. This is an attempt to make something work
		// for WebSphere. They may be using some kind of Servlet loader that
		// needs an absolute path based search
		if (stream == null) {
			stream = JacobReleaseInfo.class.getClassLoader().getResourceAsStream("/" + PROPERTY_FILE_NAME);
		}
		// A report came in that WebSphere had trouble finding the file
		// so lets trap it. Plus, it's a good idea anyway.
		if (stream == null) {
			throw new IllegalStateException("Can't find " + PROPERTY_FILE_NAME + " using JacobReleaseInfo.class.getClassLoader().getResourceAsStream()");
		}
		else {
			try {
				versionProps.load(stream);
				stream.close();
				buildVersion = (String) versionProps.get("version");
				buildDate = (String) versionProps.get("build.date");
			}
			catch (IOException ioe) {
				ioe.printStackTrace();
				System.err.println("Warning! Couldn't load props " + ioe);
			}
		}
	}

	/**
	 * loads PROPERT_FILE_NAME and returns the value of version in it
	 * 
	 * @return String value of version in PROPERT_FILE_NAME or "" if none
	 */
	public static String getBuildDate() {
		if (buildDate.equals("")) {
			loadVersionProperties();
		}
		return buildDate;
	}

	/**
	 * loads PROPERT_FILE_NAME and returns the value of version in it
	 * 
	 * @return String value of version in PROPERT_FILE_NAME or "" if none
	 */
	public static String getBuildVersion() {
		if (buildVersion.equals("")) {
			loadVersionProperties();
		}
		return buildVersion;
	}

}
