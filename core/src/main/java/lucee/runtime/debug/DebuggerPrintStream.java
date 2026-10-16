package lucee.runtime.debug;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * PrintStream wrapper that tees output to the DebuggerListener.
 * Installed on System.out/err when DEBUGGER_DAP_SECRET is set.
 * Always passes through to the original stream; only notifies listener when active.
 *
 * Uses an OutputStream wrapper because PrintStream.print/println use a private
 * internal write path (BufferedWriter → OutputStreamWriter → OutputStream) that
 * bypasses the public write(byte[]) methods. Wrapping the OutputStream ensures
 * ALL output paths are intercepted.
 */
public class DebuggerPrintStream extends PrintStream {

	public DebuggerPrintStream(PrintStream original, boolean isStdErr) {
		super(new NotifyingOutputStream(original, isStdErr), true);
	}

	/**
	 * OutputStream that forwards all writes to the original stream
	 * and notifies the DebuggerListener of new output.
	 */
	private static class NotifyingOutputStream extends FilterOutputStream {
		private final boolean isStdErr;

		NotifyingOutputStream(OutputStream original, boolean isStdErr) {
			super(original);
			this.isStdErr = isStdErr;
		}

		@Override
		public void write(byte[] buf, int off, int len) throws IOException {
			out.write(buf, off, len);
			notifyListener(new String(buf, off, len));
		}

		@Override
		public void write(int b) throws IOException {
			out.write(b);
			notifyListener(String.valueOf((char) b));
		}

		private void notifyListener(String text) {
			if (text == null || text.isEmpty()) return;
			DebuggerListener listener = DebuggerRegistry.getListener();
			if (listener != null && listener.isClientConnected()) {
				listener.onOutput(text, isStdErr);
			}
		}
	}
}
