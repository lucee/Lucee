package lucee.loader.servlet.jakarta;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.http.Part;

public class PartJavax implements Part {

	private jakarta.servlet.http.Part part;

	public PartJavax(jakarta.servlet.http.Part part) {
		this.part = part;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return part.getInputStream();
	}

	@Override
	public String getContentType() {
		return part.getContentType();
	}

	@Override
	public String getName() {
		return part.getName();
	}

	@Override
	public String getSubmittedFileName() {
		return part.getSubmittedFileName();
	}

	@Override
	public long getSize() {
		return part.getSize();
	}

	@Override
	public void write(String fileName) throws IOException {
		part.write(fileName);
	}

	@Override
	public void delete() throws IOException {
		part.delete();
	}

	@Override
	public String getHeader(String name) {
		return part.getHeader(name);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return part.getHeaders(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return part.getHeaderNames();
	}
}
