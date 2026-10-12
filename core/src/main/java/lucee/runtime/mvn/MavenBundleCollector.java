package lucee.runtime.mvn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourcesImpl;
import lucee.runtime.mvn.MavenUtil.GAVSO;

/**
 * Build time tool. Reads the Maven coordinates declared by (lite) extensions, resolves them
 * including all transitive dependencies and stores everything in repository layout inside a
 * directory, together with the .sha1 files and an ".index" file as read by
 * {@link MavenDeployer#deployBundled(Resource, ClassLoader, Log)}.
 * 
 * Usage: MavenBundleCollector &lt;core MANIFEST.MF&gt; &lt;cache directory&gt; &lt;target directory&gt; [repository url]...
 */
public final class MavenBundleCollector {

	private static final Pattern MAVEN_ENTRY = Pattern.compile("['\"](?:maven|mvn)['\"]\\s*:\\s*['\"]([^'\"]+)['\"]", Pattern.CASE_INSENSITIVE);

	private MavenBundleCollector() {
	}

	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.err.println("usage: MavenBundleCollector <core MANIFEST.MF> <cache directory> <target directory> [repository url]...");
			System.exit(2);
		}
		File manifest = new File(args[0]);
		File cache = new File(args[1]);
		File target = new File(args[2]);
		List<String> repos = new ArrayList<>();
		for (int i = 3; i < args.length; i++) {
			repos.add(args[i].endsWith("/") ? args[i] : args[i] + "/");
		}
		if (repos.isEmpty()) repos.add("https://repo1.maven.org/maven2/");

		List<File> lexes = new ArrayList<>();
		cache.mkdirs();
		for (String[] gav: readRequiredExtensions(manifest)) {
			File lite = downloadLite(gav[0], gav[1], gav[2], cache, repos);
			if (lite == null) System.out.println("WARN: no lite version available for [" + gav[0] + ":" + gav[1] + ":" + gav[2] + "], no maven dependencies collected for it");
			else lexes.add(lite);
		}
		int count = collect(target, lexes);
		System.out.println("collected [" + count + "] maven files in [" + target + "]");
	}

	/**
	 * @return group, artifact and version of every entry of Require-Extension
	 */
	static List<String[]> readRequiredExtensions(File manifestFile) throws IOException {
		Manifest mf;
		try (InputStream is = Files.newInputStream(manifestFile.toPath())) {
			mf = new Manifest(is);
		}
		String str = mf.getMainAttributes().getValue("Require-Extension");
		if (str == null) throw new IOException("no [Require-Extension] defined in [" + manifestFile + "]");
		List<String[]> result = new ArrayList<>();
		for (String entry: str.split(",")) {
			String[] gav = entry.split(";")[0].trim().split(":");
			if (gav.length < 3) continue;
			result.add(new String[] { gav[0].trim(), gav[1].trim(), gav[2].trim() });
		}
		return result;
	}

	private static File downloadLite(String group, String artifact, String version, File cache, List<String> repos) throws IOException {
		File dest = new File(cache, artifact + "-" + version + "-lite.lex");
		if (dest.isFile() && dest.length() > 0) return dest;
		for (String repo: repos) {
			String url = repo + group.replace('.', '/') + "/" + artifact + "/" + version + "/" + artifact + "-" + version + "-lite.lex";
			System.out.println("download " + url);
			try (InputStream is = new URL(url).openStream()) {
				Files.copy(is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
				return dest;
			}
			catch (IOException ioe) {
				System.out.println("  not available: " + ioe.getMessage());
			}
		}
		return null;
	}

	public static int collect(File target, List<File> extensions) throws IOException {
		Set<GAVSO> coordinates = new LinkedHashSet<>();
		for (File lex: extensions) {
			List<GAVSO> list = readCoordinates(lex);
			System.out.println("extension [" + lex.getName() + "] declares [" + list.size() + "] maven dependenc" + (list.size() == 1 ? "y" : "ies"));
			coordinates.addAll(list);
		}

		target.mkdirs();
		Resource dir = ResourcesImpl.getFileResourceProvider().getResource(target.getAbsolutePath());
		Log log = consoleLog();
		for (GAVSO g: coordinates) {
			POM pom = POM.getInstance(dir, g.g, g.a, g.v, POM.SCOPES_FOR_RUNTIME, log);
			pom.downloadArtifact();
			for (POM dep: pom.getAllDependencies(false)) {
				dep.downloadArtifact();
			}
		}
		return writeIndex(target.toPath());
	}

	static List<GAVSO> readCoordinates(File lex) throws IOException {
		List<GAVSO> result = new ArrayList<>();
		try (ZipFile zip = new ZipFile(lex)) {
			ZipEntry entry = zip.getEntry("META-INF/MANIFEST.MF");
			if (entry == null) throw new IOException("no manifest found in [" + lex + "]");
			Manifest mf;
			try (InputStream is = zip.getInputStream(entry)) {
				mf = new Manifest(is);
			}
			for (Map.Entry<Object, Object> e: mf.getMainAttributes().entrySet()) {
				String name = ((Attributes.Name) e.getKey()).toString();
				String value = (String) e.getValue();
				if ("maven".equalsIgnoreCase(name) || "mvn".equalsIgnoreCase(name)) {
					List<GAVSO> list = MavenUtil.toGAVSOs(unwrap(value), null);
					if (list != null) result.addAll(list);
					continue;
				}
				Matcher m = MAVEN_ENTRY.matcher(value);
				while (m.find()) {
					GAVSO g = MavenUtil.toGAVSO(m.group(1), null);
					if (g != null) result.add(g);
				}
			}
		}
		return result;
	}

	private static String unwrap(String str) {
		str = str.trim();
		if (str.length() > 1 && (str.charAt(0) == '"' || str.charAt(0) == '\'') && str.charAt(str.length() - 1) == str.charAt(0)) return str.substring(1, str.length() - 1);
		return str;
	}

	private static int writeIndex(Path root) throws IOException {
		List<String> files;
		try (Stream<Path> walk = Files.walk(root)) {
			files = walk.filter(Files::isRegularFile).map(p -> root.relativize(p).toString().replace(File.separatorChar, '/')).filter(p -> !p.endsWith(".sha1") && !p.startsWith("."))
					.filter(p -> !p.endsWith(".lastUpdated") && !p.endsWith("maven-metadata.xml")).collect(Collectors.toList());
		}
		Collections.sort(files);
		StringBuilder index = new StringBuilder();
		for (String f: files) {
			Files.write(root.resolve(f + ".sha1"), sha1(Files.readAllBytes(root.resolve(f))).getBytes(StandardCharsets.UTF_8));
			if (index.length() > 0) index.append(';');
			index.append(f).append(';').append(f).append(".sha1");
		}
		Files.write(root.resolve(".index"), index.toString().getBytes(StandardCharsets.UTF_8));
		return files.size();
	}

	private static String sha1(byte[] data) throws IOException {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-1").digest(data);
			StringBuilder sb = new StringBuilder();
			for (byte b: digest) {
				sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
			}
			return sb.toString();
		}
		catch (java.security.NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}

	private static Log consoleLog() {
		return (Log) Proxy.newProxyInstance(Log.class.getClassLoader(), new Class[] { Log.class }, (proxy, method, a) -> {
			String n = method.getName();
			if ((n.equals("error") || n.equals("warn") || n.equals("info")) && a != null && a.length >= 2 && a[1] instanceof String) {
				System.out.println(n + ": " + a[1]);
			}
			Class<?> rt = method.getReturnType();
			if (rt == int.class) return 0;
			if (rt == boolean.class) return false;
			return null;
		});
	}
}
