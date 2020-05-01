package lucee.runtime.type.util;

import java.io.IOException;

import lucee.print;

public class ListParser {
	private int pos = 0;
	private final int len;
	private final String str;
	private final char delimeter;
	private final char quote;
	private final ListParserConsumer consumer;
	private final boolean ignoreEmpty;
	private final boolean quoteRequired;

	public ListParser(String str, ListParserConsumer consumer) {
		this(str, consumer, ',', '"', true, false);
	}

	public ListParser(String str, ListParserConsumer consumer, char delimeter) {
		this(str, consumer, delimeter, '"', true, false);
	}

	public ListParser(String str, ListParserConsumer consumer, char delimeter, char quote) {
		this(str, consumer, delimeter, '"', true, false);
	}

	public ListParser(String str, ListParserConsumer consumer, char delimeter, char quote, boolean ignoreEmpty) {
		this(str, consumer, delimeter, '"', ignoreEmpty, false);
	}

	public ListParser(String str, ListParserConsumer consumer, char delimeter, char quote, boolean ignoreEmpty, boolean quoteRequired) {
		this.str = str;
		this.consumer = consumer;
		this.delimeter = delimeter;
		this.quote = quote;
		this.ignoreEmpty = ignoreEmpty;
		this.quoteRequired = quoteRequired;
		this.len = str.length();
	}

	public void parse() throws IOException {
		del();
		while (pos < len) {
			entry();
			if (pos >= len) break;
			else if (str.charAt(pos) == delimeter) {
				pos++;
				del();
			}
			else throw new IOException("Invalid Syntax at [" + (pos + 1) + "]: unexpected end");
		}
	}

	// [0=a][1=,][2=,]
	private void del() {
		remws();
		while (pos < len && str.charAt(pos) == delimeter) {
			pos++;
			if (!ignoreEmpty) consumer.entry("");
			remws();
		}
		if (pos == len && !ignoreEmpty) consumer.entry("");
	}

	private void entry() throws IOException {
		char c = str.charAt(pos);
		// read until we reach ending quote
		if (c == quote) quoted();

		// read until we reach delimeter or whitespace
		else {
			if (quoteRequired) throw new IOException("Invalid Syntax at [" + (pos + 1) + "]: all values must be between quotes");
			unquoted();
		}

		remws();

	}

	private void unquoted() {
		char c = str.charAt(pos);
		pos++;
		StringBuilder sb = new StringBuilder();
		sb.append(c);
		while (pos < len) {
			c = str.charAt(pos);
			// print.e("=>" + c);
			if (c == delimeter || Character.isWhitespace(c)) break;
			sb.append(c);
			pos++;
		}
		consumer.entry(sb.toString());
	}

	private void quoted() throws IOException {
		int start = pos;
		pos++;
		char c = 0;
		StringBuilder sb = new StringBuilder();
		while (pos < len) {
			c = str.charAt(pos);
			if (c == quote) {
				// escape
				if (pos + 1 < len && str.charAt(pos + 1) == quote) {
					pos++;
				}
				else {
					pos++;
					break;
				}
			}
			sb.append(c);
			pos++;
		}
		if (pos == len && c != quote) {
			if (quoteRequired) throw new IOException("Invalid Syntax at [" + (pos + 1) + "]: missing ending quote [" + quote + "]");
			pos = start;
			unquoted();
			return;
		}
		consumer.entry(sb.toString());
	}

	private void remws() {
		while (pos < len) {
			if (!Character.isWhitespace(str.charAt(pos))) break;
			pos++;
		}
	}

	public static void main(String[] args) throws IOException {
		new ListParser("'a','b,c", new SimpleConsumer(), ',', '\'', false, false).parse();
		// new ListParser(",,a , ,, b", new SimpleConsumer(), ',', '\'', false).parse();
		// new ListParser(" 'a''b c\nd' ", new SimpleConsumer(), ',', '\'').parse();
		// new ListParser("'a''b c\nd' , , abcd,1234, 'aaa' ,'789' ", new SimpleConsumer(), ',',
		// '\'').parse();
	}

	public static class SimpleConsumer implements ListParserConsumer {

		@Override
		public void entry(String str) {
			print.e("entry:" + str + ":");
		}

	}
}
