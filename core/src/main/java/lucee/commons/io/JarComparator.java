package lucee.commons.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarComparator {

	public void compareJarFiles(File jarFile1, File jarFile2) throws IOException, NoSuchAlgorithmException {
		Map<String, String> jar1Contents = getJarContents(jarFile1);
		Map<String, String> jar2Contents = getJarContents(jarFile2);
		// print.e(jar2Contents);
		// Compare the contents of the two JAR files
		compareJars(jar1Contents, jar2Contents);
	}

	private Map<String, String> getJarContents(File jarFile) throws IOException, NoSuchAlgorithmException {
		Map<String, String> contents = new HashMap<>();
		try (ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(jarFile))) {
			ZipEntry entry;
			while ((entry = zipInputStream.getNextEntry()) != null) {
				if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
					contents.put(entry.getName(), computeClassHash(zipInputStream));
				}
			}
		}
		return contents;
	}

	private String computeClassHash(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		bis.mark(Integer.MAX_VALUE);
		// Skip the first 8 bytes to ignore the magic number and the version number
		long skipped = bis.skip(8);
		if (skipped != 8) {
			throw new IOException("Failed to skip the initial 8 bytes of the class file");
		}

		byte[] bytesBuffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = bis.read(bytesBuffer)) != -1) {
			digest.update(bytesBuffer, 0, bytesRead);
		}

		// Reset the stream for any subsequent reads
		bis.reset();

		return bytesToHex(digest.digest());
	}

	private String bytesToHex(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (byte b: bytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}

	private void compareJars(Map<String, String> jar1Contents, Map<String, String> jar2Contents) {
		System.out.println("Comparing JAR files...");

		for (String file: jar1Contents.keySet()) {
			if (jar2Contents.containsKey(file)) {
				String hash1 = jar1Contents.get(file);
				String hash2 = jar2Contents.get(file);
				if (!hash1.equals(hash2)) {
					System.out.println("File differs: " + file);
				}
			}
		}

		System.out.println("Comparison complete.");
	}

	public static void main(String[] args) {
		try {
			JarComparator comparator = new JarComparator();
			comparator.compareJarFiles(new File("/Users/mic/Projects/Lucee/Lucee6/loader/target/6.1.0.82-SNAPSHOT.jvm11.lco"),
					new File("/Users/mic/Projects/Lucee/Lucee6/loader/target/6.1.0.82-SNAPSHOT.jvm21.lco"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}