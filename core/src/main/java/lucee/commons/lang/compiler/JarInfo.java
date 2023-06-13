package lucee.commons.lang.compiler;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ClassUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.op.Caster;

public class JarInfo {

	private static final byte[] MAJOR_VERSIONS = new byte[100];

	static {
		// java version one has a byte value of "45", java 2 "46" and so one, so that we have java 1 at
		// position 1, we set an offset of 44
		byte tmp = (byte) 44;
		for (byte i = 0; i < 100; i++) {
			MAJOR_VERSIONS[i] = tmp++;
		}
	}

	public static int getMajorVersion(PageContext pc, String path) throws IOException, ExpressionException {
		return getMajorVersion(Caster.toResource(pc, path, true));
	}

	public static int getMajorVersion(Resource jar) throws IOException {
		ZipInputStream zis = null;
		try {
			// read the first 8 bytes of the first class listed
			zis = new ZipInputStream(IOUtil.toBufferedInputStream(jar.getInputStream()));
			ZipEntry entry;
			byte[] data = null;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().endsWith(".class")) {
					byte[] buffer = new byte[9];
					if (zis.read(buffer) == 9 && ClassUtil.isRawBytecode(buffer)) {

						data = buffer;
					}
				}
				zis.closeEntry();
				if (data != null) break;
			}
			if (data == null && data.length < 8) throw new IOException("could not find a class to read in the jar [" + jar + "]");
			for (int i = 0; i < MAJOR_VERSIONS.length; i++) {

				if (data[7] == MAJOR_VERSIONS[i]) return i;
			}
			throw new IOException("could not read major version from class file in the jar [" + jar + "]");

		}
		finally {
			IOUtil.close(zis);
		}
	}
}
