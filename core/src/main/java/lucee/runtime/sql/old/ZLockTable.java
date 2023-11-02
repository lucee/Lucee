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

import java.util.Vector;

// Referenced classes of package Zql:
//            ZStatement

public final class ZLockTable implements ZStatement {

	public ZLockTable() {
		nowait_ = false;
		lockMode_ = null;
		tables_ = null;
	}

	public void addTables(Vector vector) {
		tables_ = vector;
	}

	public Vector getTables() {
		return tables_;
	}

	public void setLockMode(String s) {
		lockMode_ = new String(s);
	}

	public String getLockMode() {
		return lockMode_;
	}

	public boolean isNowait() {
		return nowait_;
	}

	boolean nowait_;
	String lockMode_;
	Vector tables_;
}