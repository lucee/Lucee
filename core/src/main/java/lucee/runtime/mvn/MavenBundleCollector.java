package lucee.runtime.mvn;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Build time tool. Takes the "maven" folder (repository layout) of every extension defined as lite
 * in Require-Extension (group:artifact:version:lite) and copies it into one directory, together with the .sha1 files and an ".index"
 * file as read by {@link MavenDeployer#deployBundled}.
 * 
 * Usage: MavenBundleCollector &lt;core MANIFEST.MF&gt; &lt;cache directory&gt; &lt;target directory&gt; [extensions directory] [repository url]...
 * 
 * The full extension is taken from the cache directory (mvn_group_artifact_version.lex) and
 * downloaded from the repositories if it is not there.
 */
public final class MavenBundleCollector {

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
			if (!args[i].contains("://")) continue;
			repos.add(args[i].endsWith("/") ? args[i] : args[i] + "/");
		}
		if (repos.isEmpty()) throw new IOException("no repository url defined, pass at least one repository url as argument");

		File extensions = args.length > 3 && !args[3].contains("://") ? new File(args[3]) : null;

		cache.mkdirs();
		target.mkdirs();
		for (String[] gav: readRequiredExtensions(manifest)) {
			// only extensions defined as lite (group:artifact:version:lite) need their maven files bundled separately
			if (!"lite".equals(gav[3])) continue;
			File lex = getExtension(gav[0], gav[1], gav[2], cache, repos, "");
			int count = copyMavenFolder(lex, target);
			System.out.println("extension [" + lex.getName() + "] provides [" + count + "] maven file(s)");
			if (extensions == null) continue;

			// the lite extension is bundled under the file name of the full extension
			File bundled = new File(extensions, lex.getName());
			if (!bundled.isFile()) throw new IOException("bundled extension [" + bundled + "] not found");
			File lite = getExtension(gav[0], gav[1], gav[2], new File(cache, "lite"), repos, "-lite");
			if (lite == null) throw new IOException("extension [" + gav[0] + ":" + gav[1] + ":" + gav[2] + "] is defined as lite, but no lite version is available");
			Files.copy(lite.toPath(), bundled.toPath(), StandardCopyOption.REPLACE_EXISTING);
			System.out.println("  bundling [" + lite.getName() + "] (" + lite.length() + " bytes instead of " + lex.length() + ")");
		}
		System.out.println("collected [" + writeIndex(target.toPath()) + "] maven files in [" + target + "]");
	}

	/**
	 * @return group, artifact, version and "lite" (or empty) of every entry of Require-Extension
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
			result.add(new String[] { gav[0].trim(), gav[1].trim(), gav[2].trim(), gav.length > 3 && "lite".equalsIgnoreCase(gav[3].trim()) ? "lite" : "" });
		}
		return result;
	}

	private static File getExtension(String group, String artifact, String version, File cache, List<String> repos, String classifier) throws IOException {
		boolean lite = !classifier.isEmpty();
		cache.mkdirs();
		File file = new File(cache, "mvn_" + group + "_" + artifact + "_" + version + classifier + ".lex");
		if (file.isFile() && file.length() > 0) return file;
		StringBuilder tried = new StringBuilder();
		for (String repo: repos) {
			String url = repo + group.replace('.', '/') + "/" + artifact + "/" + version + "/" + artifact + "-" + version + classifier + ".lex";
			System.out.println("download " + url);
			try (InputStream is = new URL(url).openStream()) {
				Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				return file;
			}
			catch (IOException ioe) {
				Files.deleteIfExists(file.toPath());
				tried.append("\n- ").append(url);
			}
		}
		if (lite) return null;
		throw new IOException("could not download extension [" + group + ":" + artifact + ":" + version + "], tried:" + tried);
	}

	private static int copyMavenFolder(File lex, File target) throws IOException {
		int count = 0;
		Path root = target.toPath().toAbsolutePath().normalize();
		try (ZipFile zip = new ZipFile(lex)) {
			Enumeration<? extends ZipEntry> entries = zip.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				String name = entry.getName();
				if (entry.isDirectory() || !name.startsWith("maven/") || name.endsWith(".sha1")) continue;
				String rel = name.substring(6);
				// extensions may contain files outside the repository layout (for example a copy of their own pom
				// directly in "maven/"), those cannot be resolved by Maven and are ignored
				if (!MavenDeployer.isRepositoryPath(rel)) {
					System.out.println("  ignoring [" + name + "] in [" + lex.getName() + "], not in group/artifact/version layout");
					continue;
				}
				Path dest = root.resolve(rel).normalize();
				if (!dest.startsWith(root)) throw new IOException("invalid entry [" + name + "] in [" + lex + "]");
				byte[] data;
				try (InputStream is = zip.getInputStream(entry)) {
					data = is.readAllBytes();
				}
				if (Files.isRegularFile(dest) && !java.util.Arrays.equals(Files.readAllBytes(dest), data)) {
					throw new IOException("conflicting content for [" + name.substring(6) + "] provided by [" + lex.getName() + "]");
				}
				Files.createDirectories(dest.getParent());
				Files.write(dest, data);
				count++;
			}
		}
		return count;
	}

	private static int writeIndex(Path root) throws IOException {
		List<String> files;
		try (Stream<Path> walk = Files.walk(root)) {
			files = walk.filter(Files::isRegularFile).map(p -> root.relativize(p).toString().replace(File.separatorChar, '/')).filter(p -> !p.endsWith(".sha1") && !p.startsWith("."))
					.collect(Collectors.toList());
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
}
