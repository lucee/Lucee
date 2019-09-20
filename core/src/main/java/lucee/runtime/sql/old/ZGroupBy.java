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

package lucee.runtime.sql.old;

import java.io.Serializable;
import java.util.Vector;

// Referenced classes of package Zql:
//            ZExp

public final class ZGroupBy implements Serializable {

	public ZGroupBy(Vector vector) {
		having_ = null;
		groupby_ = vector;
	}

	public void setHaving(ZExp zexp) {
		having_ = zexp;
	}

	public Vector getGroupBy() {
		return groupby_;
	}

	public ZExp getHaving() {
		return having_;
	}

	@Override
	public String toString() {
		StringBuffer stringbuffer = new StringBuffer("group by ");
		stringbuffer.append(groupby_.elementAt(0).toString());
		for (int i = 1; i < groupby_.size(); i++)
			stringbuffer.append(", " + groupby_.elementAt(i).toString());

		if (having_ != null) stringbuffer.append(" having " + having_.toString());
		return stringbuffer.toString();
	}

	Vector groupby_;
	ZExp having_;
}