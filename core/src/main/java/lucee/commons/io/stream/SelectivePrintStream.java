package lucee.commons.io.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class SelectivePrintStream extends PrintStream {

	private final PrintStream originalPrintStream;
	private final PrintStream interceptedOut;
	private final long creatingThreadId;
	private final ByteArrayOutputStream baos;

	public SelectivePrintStream(PrintStream originalPrintStream) {
		super(originalPrintStream);
		this.originalPrintStream = originalPrintStream;
		creatingThreadId = Thread.currentThread().getId();
		baos = new ByteArrayOutputStream();
		interceptedOut = new PrintStream(baos);
	}

	@Override
	public void flush() {
		if (intercept()) {
			interceptedOut.flush();
		}
		else {
			originalPrintStream.flush();
		}
	}

	@Override
	public void close() {
		if (intercept()) {
			interceptedOut.close();
		}
		else {
			originalPrintStream.close();
		}
	}

	@Override
	public boolean checkError() {
		if (intercept()) {
			return interceptedOut.checkError();
		}
		else {
			return originalPrintStream.checkError();
		}
	}

	@Override
	public void print(boolean b) {
		if (intercept()) {
			interceptedOut.print(b);
		}
		else {
			originalPrintStream.print(b);
		}
	}

	@Override
	public void print(char c) {
		if (intercept()) {
			interceptedOut.print(c);
		}
		else {
			originalPrintStream.print(c);
		}
	}

	@Override
	public void print(int i) {
		if (intercept()) {
			interceptedOut.print(i);
		}
		else {
			originalPrintStream.print(i);
		}
	}

	@Override
	public void print(long l) {
		if (intercept()) {
			interceptedOut.print(l);
		}
		else {
			originalPrintStream.print(l);
		}
	}

	@Override
	public void print(float f) {
		if (intercept()) {
			interceptedOut.print(f);
		}
		else {
			originalPrintStream.print(f);
		}
	}

	@Override
	public void print(double d) {
		if (intercept()) {
			interceptedOut.print(d);
		}
		else {
			originalPrintStream.print(d);
		}
	}

	@Override
	public void print(char[] s) {
		if (intercept()) {
			interceptedOut.print(s);
		}
		else {
			originalPrintStream.print(s);
		}
	}

	@Override
	public void print(String s) {
		if (intercept()) {
			interceptedOut.print(s);
		}
		else {
			originalPrintStream.print(s);
		}
	}

	@Override
	public void print(Object obj) {
		if (intercept()) {
			interceptedOut.print(obj);
		}
		else {
			originalPrintStream.print(obj);
		}
	}

	@Override
	public void println() {
		if (intercept()) {
			interceptedOut.println();
		}
		else {
			originalPrintStream.println();
		}
	}

	@Override
	public void println(boolean x) {
		if (intercept()) {
			interceptedOut.println(x);
		}
		else {
			originalPrintStream.println(x);
		}
	}

	@Override
	public void println(char x) {
		if (intercept()) {
			interceptedOut.println(x);
		}
		else {
			originalPrintStream.println(x);
		}
	}

	@Override
	public void println(int x) {
		if (intercept()) {
			interceptedOut.println(x);
		}
		else {
			originalPrintStream.println(x);
		}
	}

	@Override
	public void println(long x) {
		if (intercept()) {
			interceptedOut.println(x);
		}
		else {
			originalPrintStream.println(x);
		}
	}

	@Override
	public void println(float x) {
		if (intercept()) {
			interceptedOut.println(x);
		}
		else {
			originalPrintStream.println(x);
		}
	}

	@Override
	public void println(double x) {
		if (intercept()) {
			interceptedOut.println(x);
		}
		else {
			originalPrintStream.println(x);
		}
	}

	@Override
	public void println(char[] x) {
		if (intercept()) {
			interceptedOut.println(x);
		}
		else {
			originalPrintStream.println(x);
		}
	}

	@Override
	public void println(String x) {
		if (intercept()) {
			interceptedOut.println(x);
		}
		else {
			originalPrintStream.println(x);
		}
	}

	@Override
	public void println(Object x) {
		if (intercept()) {
			interceptedOut.println(x);
		}
		else {
			originalPrintStream.println(x);
		}
	}

	@Override
	public PrintStream printf(String format, Object... args) {
		if (intercept()) {
			interceptedOut.printf(format, args);
			return this;
		}
		else {
			originalPrintStream.printf(format, args);
			return this;
		}
	}

	@Override
	public PrintStream printf(Locale l, String format, Object... args) {
		if (intercept()) {
			interceptedOut.printf(l, format, args);
			return this;
		}
		else {
			originalPrintStream.printf(l, format, args);
			return this;
		}
	}

	@Override
	public PrintStream format(String format, Object... args) {
		if (intercept()) {
			interceptedOut.format(format, args);
			return this;
		}
		else {
			originalPrintStream.format(format, args);
			return this;
		}
	}

	@Override
	public PrintStream format(Locale l, String format, Object... args) {
		if (intercept()) {
			interceptedOut.format(l, format, args);
			return this;
		}
		else {
			originalPrintStream.format(l, format, args);
			return this;
		}
	}

	@Override
	public PrintStream append(CharSequence csq) {
		if (intercept()) {
			interceptedOut.append(csq);
			return this;
		}
		else {
			originalPrintStream.append(csq);
			return this;
		}
	}

	@Override
	public PrintStream append(CharSequence csq, int start, int end) {
		if (intercept()) {
			interceptedOut.append(csq, start, end);
			return this;
		}
		else {
			originalPrintStream.append(csq, start, end);
			return this;
		}
	}

	@Override
	public PrintStream append(char c) {
		if (intercept()) {
			interceptedOut.append(c);
			return this;
		}
		else {
			originalPrintStream.append(c);
			return this;
		}
	}

	@Override
	public void write(byte[] b) throws IOException {
		if (intercept()) {
			interceptedOut.write(b);
		}
		else {
			originalPrintStream.write(b);
		}
	}

	@Override
	public void write(int b) {
		if (intercept()) {
			interceptedOut.write(b);
		}
		else {
			originalPrintStream.write(b);
		}
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		if (intercept()) {
			interceptedOut.write(buf, off, len);
		}
		else {
			originalPrintStream.write(buf, off, len);
		}
	}

	public String getInterceptedOutput() {
		try {
			return baos.toString("UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			return baos.toString();
		}
	}

	private boolean intercept() {
		return Thread.currentThread().getId() == creatingThreadId;
	}
}