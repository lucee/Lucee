/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.transformer.cfml.evaluator.impl;

import java.nio.charset.Charset;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.CharsetX;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.Position;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;

public final class ProcessingDirectiveException extends TemplateException {

	private static final long serialVersionUID = 9006344831997426192L;

	private CharsetX charset;
	private Boolean writeLog;
	private Boolean dotNotationUpperCase;

	public final Position start;
	public final Position end;

	public ProcessingDirectiveException(SourceCode cfml, Position start, Position end, Charset charset, Boolean dotNotationUpperCase, Boolean writeLog) {
		super(cfml, createMessage(cfml, charset, writeLog));
		this.start = start;
		this.end = end;
		this.charset = CharsetUtil.toCharsetX(charset);
		this.writeLog = writeLog;
		this.dotNotationUpperCase = dotNotationUpperCase;
	}

	private static String createMessage(SourceCode sc, Charset charset, Boolean writeLog) {
		StringBuilder msg = new StringBuilder();
		if (charset != null && sc instanceof PageSourceCode && !((PageSourceCode) sc).getCharset().equals(charset))
			msg.append("change charset from [" + ((PageSourceCode) sc).getCharset() + "] to [" + charset + "].");

		if (writeLog != null && sc.getWriteLog() != writeLog) msg.append("change writelog from [" + sc.getWriteLog() + "] to [" + writeLog + "].");

		return msg.toString();
	}

	public Charset getCharset() {
		return CharsetUtil.toCharset(charset);
	}

	public Boolean getDotNotationUpperCase() {
		return dotNotationUpperCase;
	}

	public Boolean getWriteLog() {
		return writeLog;
	}

}