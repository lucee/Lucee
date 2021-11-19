package lucee.commons.lang.compiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.tools.JavaFileObject;

import lucee.commons.io.IOUtil;

public class CustomJavaFileObject implements JavaFileObject {
	private final String binaryName;
	private final URI uri;
	private String name;
	private ByteArrayOutputStream baos;
	private Kind kind;

	public CustomJavaFileObject(String javaObjectName, URI uri, InputStream is, Kind kind) throws IOException {
		this.uri = uri;
		this.binaryName = javaObjectName;
		this.kind = kind;
		String stripName = javaObjectName;
		if (stripName.endsWith("/")) {
			stripName = stripName.substring(0, stripName.length() - 1);
		}
		name = javaObjectName.substring(javaObjectName.lastIndexOf('/') + 1);
		if (is != null) {
			baos = new ByteArrayOutputStream();
			IOUtil.copy(is, baos, false, true);
		}
	}

	@Override
	public URI toUri() {
		return uri;
	}

	@Override
	public InputStream openInputStream() throws IOException {
		final byte[] byteArray = baos.toByteArray();
		return new ByteArrayInputStream(byteArray);
	}

	@Override
	public OutputStream openOutputStream() throws IOException {
		baos = new ByteArrayOutputStream();
		return baos;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
		return new InputStreamReader(openInputStream());
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
		if (baos != null) {
			byte[] b = baos.toByteArray();
			return new String(b);
		}
		throw new UnsupportedOperationException();
	}

	@Override
	public Writer openWriter() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getLastModified() {
		return 0;
	}

	@Override
	public boolean delete() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Kind getKind() {
		return kind;
	}

	public void setKind(Kind k) {
		this.kind = k;
	}

	@Override
	public boolean isNameCompatible(String simpleName, Kind kind) {
		String baseName = simpleName + kind.extension;
		return kind.equals(getKind()) && (baseName.equals(getName()) || getName().endsWith("/" + baseName));
	}

	@Override
	public NestingKind getNestingKind() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Modifier getAccessLevel() {
		throw new UnsupportedOperationException();
	}

	public String binaryName() {
		return binaryName;
	}

	@Override
	public String toString() {
		return "CustomJavaFileObject{" + "uri=" + uri + '}';
	}

	public int getSize() {
		if (baos == null) {
			return -1;
		}
		else {
			return baos.size();
		}

	}
}