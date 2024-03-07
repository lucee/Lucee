package lucee.commons.lang.compiler.janino;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.codehaus.commons.compiler.util.resource.ResourceCreator;

public class ResourceCreatorImpl implements ResourceCreator {

	private ByteArrayOutputStream baos;

	@Override
	public OutputStream createResource(String name) throws IOException {
		baos = new ByteArrayOutputStream();
		return baos;
	}

	@Override
	public boolean deleteResource(String name) {
		return true;
	}

	public byte[] getBytes(boolean release) {
		byte[] tmp = baos.toByteArray();
		if (release) release();
		return tmp;
	}

	public void release() {
		baos = null;
	}

}
