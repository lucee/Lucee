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

package lucee.runtime.exp;

import lucee.runtime.config.Config;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;

/**
 *
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
public class ExpressionException extends PageExceptionImpl {

	private static final Collection.Key ERR_NUMBER = KeyImpl.getInstance("ErrNumber");

	/**
	 * Class Constuctor
	 * 
	 * @param message error message
	 */
	public ExpressionException(String message) {
		super(message, "expression");
	}

	/**
	 * Class Constuctor
	 * 
	 * @param message error message
	 * @param detail detailed error message
	 */
	public ExpressionException(String message, String detail) {
		super(message, "expression");
		setDetail(detail);
	}

	@Override
	public CatchBlock getCatchBlock(Config config) {
		CatchBlock sct = super.getCatchBlock(config);
		sct.setEL(ERR_NUMBER, new Double(0));
		return sct;
	}

	/**
	 * @param e
	 * @return pageException
	 */
	public static ExpressionException newInstance(Exception e) {
		if (e instanceof ExpressionException) return (ExpressionException) e;
		else if (e instanceof PageException) {
			PageException pe = (PageException) e;
			ExpressionException ee = new ExpressionException(pe.getMessage());
			ee.detail = pe.getDetail();
			ee.setStackTrace(pe.getStackTrace());
			return ee;
		}
		else {
			ExpressionException ee = new ExpressionException(e.getMessage());
			ee.setStackTrace(e.getStackTrace());
			return ee;
		}
	}
}