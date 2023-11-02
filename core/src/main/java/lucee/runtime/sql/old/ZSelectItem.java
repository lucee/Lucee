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

// Referenced classes of package Zql:
//            ZAliasedName, ZConstant, ZUtils, ZExp

public final class ZSelectItem extends ZAliasedName {

	public ZSelectItem() {
		expression_ = null;
		aggregate_ = null;
	}

	public ZSelectItem(String s) {
		super(s, ZAliasedName.FORM_COLUMN);
		expression_ = null;
		aggregate_ = null;
		setAggregate(ZUtils.getAggregateCall(s));
	}

	public ZExp getExpression() {
		if (isExpression()) return expression_;
		if (isWildcard()) return null;
		return new ZConstant(getColumn(), 0);
	}

	public void setExpression(ZExp zexp) {
		expression_ = zexp;
		strform_ = expression_.toString();
	}

	public boolean isExpression() {
		return expression_ != null;
	}

	public void setAggregate(String s) {
		aggregate_ = s;
	}

	public String getAggregate() {
		return aggregate_;
	}

	ZExp expression_;
	String aggregate_;
}