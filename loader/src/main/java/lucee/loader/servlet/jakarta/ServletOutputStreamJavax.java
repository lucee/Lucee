package lucee.loader.servlet.jakarta;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class ServletOutputStreamJavax extends ServletOutputStream {

	private jakarta.servlet.ServletOutputStream outputStream;

	public ServletOutputStreamJavax(jakarta.servlet.ServletOutputStream outputStream) {
		this.outputStream = outputStream;
	}

	@Override
	public void write(int b) throws IOException {
		outputStream.write(b);
	}

	@Override
	public boolean isReady() {
		return outputStream.isReady();
	}

	@Override
	public void setWriteListener(javax.servlet.WriteListener writeListener) {
		outputStream.setWriteListener(new jakarta.servlet.WriteListener() {
			@Override
			public void onWritePossible() throws IOException {
				writeListener.onWritePossible();
			}

			@Override
			public void onError(Throwable t) {
				writeListener.onError(t);
			}
		});
	}
}