/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
 */
package lucee.transformer;

import lucee.runtime.PageSource;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.bytecode.BytecodeContext;

public final class TransformerException extends TemplateException {

	private static final long serialVersionUID = 6750275378601018748L;

	private Position pos;

	public TransformerException(Context c, String message, Position pos) {
		super(message);
		this.pos = pos;
		setAddional(c);

	}

	public TransformerException(Context c, Throwable cause, Position start) {
		this(c, cause.getMessage(), start);
		initCause(cause);
		setAddional(c);
	}

	public Position getPosition() {
		return pos;
	}

	private void setAddional(Context c) {
		if (c instanceof BytecodeContext) {
			BytecodeContext bc = (BytecodeContext) c;
			PageSource ps = bc.getPageSource();
			if (ps != null) setAdditional(KeyConstants._template, ps.getDisplayPath());
		}
	}
}