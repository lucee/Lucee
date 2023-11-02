package lucee.runtime.functions.string;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;

public class TrimWhiteSpace extends BIF {

	public static final char CHAR_EMPTY = 0;
	public static final char CHAR_NL = '\n';
	public static final char CHAR_SPACE = ' ';
	public static final char CHAR_TAB = '\t';
	public static final char CHAR_BS = '\b'; // \x0B\
	public static final char CHAR_FW = '\f';
	public static final char CHAR_RETURN = '\r';

	public static String call(PageContext pc, String input) {
		StringBuilder sb = new StringBuilder();
		int len = input.length();
		char charBuffer = CHAR_EMPTY, c;
		for (int i = 0; i < len; i++) {
			c = input.charAt(i);
			switch (c) {
			case CHAR_NL:
				if (charBuffer != CHAR_NL) charBuffer = c;
				break;
			case CHAR_BS:
			case CHAR_FW:
			case CHAR_RETURN:
			case CHAR_SPACE:
			case CHAR_TAB:
				if (charBuffer == CHAR_EMPTY) charBuffer = c;
				break;
			default:
				if (charBuffer != CHAR_EMPTY) {
					char b = charBuffer;// muss so bleiben!
					charBuffer = CHAR_EMPTY;
					sb.append(b);
				}
				sb.append(c);
				break;
			}
		}
		if (charBuffer != CHAR_EMPTY) {
			sb.append(charBuffer);
		}
		return sb.toString();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toString(args[0]));
		throw new FunctionException(pc, "CleanWhiteSpace", 1, 1, args.length);
	}
}