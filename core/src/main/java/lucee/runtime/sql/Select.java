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
package lucee.runtime.sql;

import java.util.ArrayList;
import java.util.List;

import lucee.runtime.sql.exp.Column;
import lucee.runtime.sql.exp.Expression;
import lucee.runtime.sql.exp.op.Operation;
import lucee.runtime.sql.exp.value.ValueNumber;

public class Select {
    private List selects = new ArrayList();
    private List froms = new ArrayList();
    private Operation where;
    private List groupbys = new ArrayList();
    private Operation having;
    private ValueNumber top;
    private boolean distinct;
    private boolean unionDistinct;

    public void addSelectExpression(Expression select) {
	selects.add(select);
	select.setIndex(selects.size());
    }

    public void addFromExpression(Column exp) {
	froms.add(exp);
	exp.setIndex(froms.size());
    }

    public void setWhereExpression(Operation where) {
	this.where = where;
    }

    public void addGroupByExpression(Column col) {
	this.groupbys.add(col);
    }

    public void setTop(ValueNumber top) {
	this.top = top;
    }

    /**
     * @return the froms
     */
    public Column[] getFroms() {
	return (Column[]) froms.toArray(new Column[froms.size()]);
    }

    /**
     * @return the groupbys
     */
    public Column[] getGroupbys() {
	if (groupbys == null) return new Column[0];
	return (Column[]) groupbys.toArray(new Column[groupbys.size()]);
    }

    /**
     * @return the havings
     */
    public Operation getHaving() {
	return having;
    }

    /**
     * @return the selects
     */
    public Expression[] getSelects() {
	return (Expression[]) selects.toArray(new Expression[selects.size()]);
    }

    /**
     * @return the where
     */
    public Operation getWhere() {
	return where;
    }

    public boolean isUnionDistinct() {
	return unionDistinct;
    }

    public boolean isDistinct() {
	return distinct;
    }

    public void setDistinct(boolean b) {
	this.distinct = b;
    }

    public void setUnionDistinct(boolean b) {
	// print.out("-"+b);
	this.unionDistinct = b;
    }

    /**
     * @param having the having to set
     */
    public void setHaving(Operation having) {
	this.having = having;
    }

    /**
     * @return the top
     */
    public ValueNumber getTop() {
	return top;
    }

}