package lucee.commons.lang;

/**
 * Single, shared scanner for SQL comments (<code>--</code> line comments and
 * <code>&#47;* ... *&#47;</code> block comments).
 *
 * <p>
 * This is the one place in the code base that knows where a SQL comment starts and ends. Every
 * consumer that needs to strip, preserve or skip comments builds on {@link #end(String, int)} so the
 * three representations of a query (the SQL sent to the database, the SQL fed to the internal
 * Query-of-Query parser, and the SQL rendered for display/logging) can never disagree about what is
 * a comment and what is not.
 * </p>
 */
public final class SQLComments {

	private SQLComments() {
		// utility class
	}

	/**
	 * If a SQL comment begins at index <code>pos</code> of <code>sql</code>, returns the index of the
	 * first character <em>after</em> the comment (equal to <code>sql.length()</code> when the comment
	 * runs to the end of the string); returns <code>-1</code> when no comment begins at that position.
	 *
	 * <p>
	 * The caller must only invoke this when <code>pos</code> is not inside a string literal &mdash;
	 * this method performs no quote tracking of its own, it only recognises a comment marker sitting at
	 * <code>pos</code>. For a line comment the returned span includes the terminating newline (if any),
	 * so that replacing the span with whitespace cannot merge the comment's line with the next one.
	 * </p>
	 *
	 * @param sql the SQL string
	 * @param pos the position to test for the start of a comment
	 * @return index just past the end of the comment, or <code>-1</code> if none starts at
	 *         <code>pos</code>
	 */
	public static int end(String sql, int pos) {
		int len = sql.length();
		// a comment marker needs at least two characters
		if (pos < 0 || pos >= len - 1) return -1;

		char c = sql.charAt(pos);
		char n = sql.charAt(pos + 1);

		// block comment /* ... */ (unterminated block runs to the end of the string)
		if (c == '/' && n == '*') {
			int e = sql.indexOf("*/", pos + 2);
			return e == -1 ? len : e + 2;
		}

		// line comment -- ... (to end of line, or end of string when there is no newline)
		if (c == '-' && n == '-') {
			int e = sql.indexOf('\n', pos + 2);
			return e == -1 ? len : e + 1; // include the newline in the span
		}

		return -1;
	}

	/**
	 * Returns <code>sql</code> with every SQL comment removed, each replaced by a single space so that
	 * the tokens on either side of a comment cannot merge. Comment markers that appear inside a string
	 * literal are left untouched. The result is trimmed.
	 *
	 * <p>
	 * Used on the paths that feed Lucee's internal parsers (the Query-of-Query {@code SelectParser} and
	 * {@code SQLPrettyfier}), which cannot handle comments.
	 * </p>
	 *
	 * @param sql the SQL to strip
	 * @return the SQL with all (non-literal) comments removed
	 */
	public static String strip(String sql) {
		int len = sql.length();
		StringBuilder sb = new StringBuilder(len);
		boolean inQuotes = false;
		char quoteType = 0;

		for (int i = 0; i < len; i++) {
			char c = sql.charAt(i);

			// only recognise comments when we are not inside a string literal
			if (!inQuotes) {
				int e = end(sql, i);
				if (e != -1) {
					sb.append(' ');
					i = e - 1; // the for-loop increment lands us on e
					continue;
				}
			}

			// track string literals so their contents are never scanned for comments
			if (c == '\'' || c == '"') {
				if (inQuotes) {
					if (c == quoteType) inQuotes = false;
				}
				else {
					quoteType = c;
					inQuotes = true;
				}
			}

			sb.append(c);
		}
		return sb.toString().trim();
	}
}
