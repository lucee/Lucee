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
package lucee.runtime.tag;

import javax.servlet.jsp.tagext.Tag;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.ext.tag.TagSupport;
import lucee.runtime.op.Caster;

public class ProcResult extends TagSupport {

	private ProcResultBean result = new ProcResultBean();

	@Override
	public void release() {
		result = new ProcResultBean();
		super.release();
	}

	/**
	 * @param maxrows The maxrows to set.
	 */
	public void setMaxrows(double maxrows) {
		result.setMaxrows(Caster.toIntValue(maxrows));
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		result.setName(name);
	}

	/**
	 * @param resultset The resultset to set.
	 * @throws ApplicationException
	 */
	public void setResultset(double resultset) throws ApplicationException {
		if (resultset < 1) throw new ApplicationException("value of attribute resultset must be a numeric value greater or equal to 1");
		result.setResultset((int) resultset);
	}

	@Override
	public int doStartTag() throws ApplicationException {

		// provide to parent
		Tag parent = getParent();
		while (parent != null && !(parent instanceof StoredProc)) {
			parent = parent.getParent();
		}

		if (parent instanceof StoredProc) {
			((StoredProc) parent).addProcResult(result);
		}
		else {
			throw new ApplicationException("Wrong Context, tag ProcResult must be inside a StoredProc tag");
		}
		return SKIP_BODY;
	}
}