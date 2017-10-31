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

import java.io.IOException;

import javax.servlet.jsp.tagext.Tag;

import lucee.commons.lang.StringUtil;
import lucee.runtime.db.SQLCaster;
import lucee.runtime.db.SQLItemImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.DatabaseException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.util.ListUtil;

/**
 * Checks the data type of a query parameter. The cfqueryparam tag is nested within a cfquery tag. It is embedded within the query SQL statement. If you specify
 * its optional parameters, cfqueryparam also performs data validation.
 *
 *
 *
 **/
public final class QueryParam extends TagImpl {

	private SQLItemImpl item = new SQLItemImpl();

	/**
	 * Specifies the character that separates values in the list of parameter values in the value attribute. The default is a comma. If you specify a list of
	 * values for the value attribute, you must also specify the list attribute.
	 */
	private String separator = ",";

	/**
	 * Yes or No. Indicates that the parameter value of the value attribute is a list of values, separated by a separator character. The default is No
	 */
	private boolean list;

	/**
	 * Maximum length of the parameter. The default value is the length of the string specified in the value attribute.
	 */
	private double maxlength = -1;

	@Override
	public void release() {
		separator = ",";
		list = false;
		maxlength = -1;
		item = new SQLItemImpl();
	}

	/**
	 * set the value list Yes or No. Indicates that the parameter value of the value attribute is a list of values, separated by a separator character. The
	 * default is No
	 * 
	 * @param list
	 *            value to set
	 **/
	public void setList(boolean list) {
		this.list = list;
	}

	/**
	 * set the value null Yes or No. Indicates whether the parameter is passed as a null. If Yes, the tag ignores the value attribute. The default is No.
	 * 
	 * @param nulls
	 *            value to set
	 **/
	public void setNull(boolean nulls) {
		item.setNulls(nulls);
	}

	/**
	 * set the value value
	 * 
	 * @param value
	 *            value to set
	 **/
	public void setValue(Object value) {
		item.setValue(value);
	}

	/**
	 * set the value maxlength Maximum length of the parameter. The default value is the length of the string specified in the value attribute.
	 * 
	 * @param maxlength
	 *            value to set
	 **/
	public void setMaxlength(double maxlength) {
		this.maxlength = maxlength;
	}

	/**
	 * set the value separator Specifies the character that separates values in the list of parameter values in the value attribute. The default is a comma. If
	 * you specify a list of values for the value attribute, you must also specify the list attribute.
	 * 
	 * @param separator
	 *            value to set
	 **/
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * set the value scale Number of decimal places of the parameter. The default value is zero.
	 * 
	 * @param scale
	 *            value to set
	 **/
	public void setScale(double scale) {
		item.setScale((int)scale);
	}

	/**
	 * set the value cfsqltype The SQL type that the parameter (any type) will be bound to.
	 * 
	 * @param type
	 *            value to set
	 * @throws DatabaseException
	 **/
	public void setCfsqltype(String type) throws DatabaseException {
		item.setType(SQLCaster.toSQLType(type));

	}

	public void setSqltype(String type) throws DatabaseException {
		item.setType(SQLCaster.toSQLType(type));

	}

	@Override
	public int doStartTag() throws PageException {
		Tag parent = getParent();
		while(parent != null && !(parent instanceof Query)) {
			parent = parent.getParent();
		}

		if(parent instanceof Query) {
			Query query = (Query)parent;

			if(!item.isNulls() && !item.isValueSet())
				throw new ApplicationException("attribute value from tag queryparam is required if attribute null is false");

			Object value = item.getValue();
			if(list || Decision.isArray(value)) {

				Array arr;

				if (Decision.isArray(value)){
					arr = Caster.toArray(value);
				}
				else {
					String v = Caster.toString(value);
					if(StringUtil.isEmpty(v)) {
						arr = new ArrayImpl();
						arr.append("");
					}
					else
						arr = ListUtil.listToArrayRemoveEmpty(v, separator);
				}

				int len = arr.size();
				StringBuffer sb = new StringBuffer();
				for (int i = 1; i <= len; i++) {
					query.setParam(item.clone(check(arr.getE(i))));
					if(i > 1)
						sb.append(',');
					sb.append('?');
				}
				write(sb.toString());
			}
			else {
				check(item.getValue());
				query.setParam(item);
				write("?");
			}
		}
		else {
			throw new ApplicationException("Wrong Context, tag QueryParam must be inside a Query tag");
		}
		return SKIP_BODY;
	}

	private Object check(Object value) throws PageException {
		if(maxlength != -1) {
			String str = Caster.toString(value);
			if(str.length() > maxlength)
				throw new DatabaseException("value [" + value + "] is to large, defined maxlength is [" + Caster.toString(maxlength)
						+ "] but length of value is [" + str.length() + "]", null, null, null);
		}
		return value;
	}

	private void write(String str) {
		try {
			pageContext.write(str);
		}
		catch (IOException e) {
		}
	}

}