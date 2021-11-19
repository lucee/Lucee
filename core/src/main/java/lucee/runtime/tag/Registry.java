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

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.registry.RegistryEntry;
import lucee.runtime.registry.RegistryQuery;
import lucee.runtime.security.SecurityManager;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * Reads, writes, and deletes keys and values in the system registry. The cfregistry tag is
 * supported on all platforms, including Linux, Solaris, and HP-UX.
 *
 *
 *
 **/
public final class Registry extends TagImpl {

	private static final short ACTION_GET_ALL = 0;
	private static final short ACTION_GET = 1;
	private static final short ACTION_SET = 2;
	private static final short ACTION_DELETE = 3;

	/**
	 * Value data to set. If you omit this attribute, cfregistry creates default value, as follows:
	 ** 
	 ** string: creates an empty string: "" dWord: creates a value of 0 (zero)
	 */
	private String value;

	/** action to the registry */
	private short action = -1;

	/**
	 * Sorts query column data (case-insensitive). Sorts on Entry, Type, and Value columns as text.
	 * Specify a combination of columns from query output, in a comma-delimited list. For example: sort
	 * = "value desc, entry asc"
	 ** 
	 ** asc: ascending (a to z) sort order desc: descending (z to a) sort order
	 */
	private String sort;

	/**
	 * string: return string values dWord: return DWord values key: return keys any: return keys and
	 * values
	 */
	private short type = RegistryEntry.TYPE_ANY;

	/** Name of a registry branch. */
	private String branch;

	/** Registry value to access. */
	private String entry;

	/** Variable into which to put value. */
	private String variable;

	/** Name of record set to contain returned keys and values. */
	private String name;

	@Override
	public void release() {
		super.release();
		value = null;
		action = -1;
		sort = null;
		type = RegistryEntry.TYPE_ANY;
		branch = null;
		entry = null;
		variable = null;
		name = null;
	}

	/**
	 * set the value value Value data to set. If you omit this attribute, cfregistry creates default
	 * value, as follows:
	 * 
	 * string: creates an empty string: "" dWord: creates a value of 0 (zero)
	 * 
	 * @param value value to set
	 **/
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * set the value action action to the registry
	 * 
	 * @param action value to set
	 * @throws ApplicationException
	 **/
	public void setAction(String action) throws ApplicationException {
		action = action.toLowerCase().trim();
		if (action.equals("getall")) this.action = ACTION_GET_ALL;
		else if (action.equals("get")) this.action = ACTION_GET;
		else if (action.equals("set")) this.action = ACTION_SET;
		else if (action.equals("delete")) this.action = ACTION_DELETE;
		else throw new ApplicationException("attribute action of the tag registry has an invalid value [" + action + "], valid values are [getAll, get, set, delete]");
	}

	/**
	 * set the value sort Sorts query column data (case-insensitive). Sorts on Entry, Type, and Value
	 * columns as text. Specify a combination of columns from query output, in a comma-delimited list.
	 * For example: sort = "value desc, entry asc"
	 * 
	 * asc: ascending (a to z) sort order desc: descending (z to a) sort order
	 * 
	 * @param sort value to set
	 **/
	public void setSort(String sort) {
		this.sort = sort;
	}

	/**
	 * set the value type string: return string values dWord: return DWord values key: return keys any:
	 * return keys and values
	 * 
	 * @param type value to set
	 * @throws ApplicationException
	 **/
	public void setType(String type) throws ApplicationException {
		type = type.toLowerCase().trim();
		if (type.equals("string")) this.type = RegistryEntry.TYPE_STRING;
		else if (type.equals("dword")) this.type = RegistryEntry.TYPE_DWORD;
		else if (type.equals("key")) this.type = RegistryEntry.TYPE_KEY;
		else if (type.equals("any")) this.type = RegistryEntry.TYPE_ANY;
		else throw new ApplicationException("attribute type of the tag registry has an invalid value [" + type + "], valid values are [string, dword]");
	}

	/**
	 * set the value branch Name of a registry branch.
	 * 
	 * @param branch value to set
	 **/
	public void setBranch(String branch) {
		this.branch = branch;
	}

	/**
	 * set the value entry Registry value to access.
	 * 
	 * @param entry value to set
	 **/
	public void setEntry(String entry) {
		this.entry = entry;
	}

	/**
	 * set the value variable Variable into which to put value.
	 * 
	 * @param variable value to set
	 **/
	public void setVariable(String variable) {
		this.variable = variable;
	}

	/**
	 * set the value name Name of record set to contain returned keys and values.
	 * 
	 * @param name value to set
	 **/
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int doStartTag() throws PageException {
		if (pageContext.getConfig().getSecurityManager().getAccess(SecurityManager.TYPE_TAG_REGISTRY) == SecurityManager.VALUE_NO)
			throw new SecurityException("can't access tag [registry]", "access is prohibited by security manager");

		if (action == ACTION_GET) doGet();
		else if (action == ACTION_GET_ALL) doGetAll();
		else if (action == ACTION_SET) doSet();
		else if (action == ACTION_DELETE) doDelete();

		return SKIP_BODY;
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doDelete() throws PageException {
		try {
			RegistryQuery.deleteValue(branch, entry);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doSet() throws PageException {
		if (entry == null) throw new ApplicationException("attribute [entry] is required for tag [registry], when action is [set]");
		if (type == RegistryEntry.TYPE_ANY) type = RegistryEntry.TYPE_STRING;
		if (value == null) {
			if (type == RegistryEntry.TYPE_DWORD) value = "0";
			else value = "";
		}

		try {
			RegistryQuery.setValue(branch, entry, type, value);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGetAll() throws PageException {
		if (name == null) throw new ApplicationException("attribute [name] is required for tag [registry], when action is [getAll]");

		try {
			RegistryEntry[] entries = RegistryQuery.getValues(branch, type);
			if (entries != null) {
				lucee.runtime.type.Query qry = new QueryImpl(new String[] { "entry", "type", "value" }, new String[] { "VARCHAR", "VARCHAR", "OTHER" }, entries.length, "query");
				for (int i = 0; i < entries.length; i++) {
					RegistryEntry e = entries[i];
					int row = i + 1;
					qry.setAt(KeyConstants._entry, row, e.getKey());
					qry.setAt(KeyConstants._type, row, RegistryEntry.toCFStringType(e.getType()));
					qry.setAt(KeyConstants._value, row, e.getValue());
				}

				// sort
				if (sort != null) {
					String[] arr = sort.toLowerCase().split(",");
					for (int i = arr.length - 1; i >= 0; i--) {
						String[] col = arr[i].trim().split("\\s+");
						if (col.length == 1) qry.sort(KeyImpl.init(col[0].trim()));
						else if (col.length == 2) {
							String order = col[1].toLowerCase().trim();
							if (order.equals("asc")) qry.sort(KeyImpl.init(col[0]), lucee.runtime.type.Query.ORDER_ASC);
							else if (order.equals("desc")) qry.sort(KeyImpl.init(col[0]), lucee.runtime.type.Query.ORDER_DESC);
							else throw new ApplicationException("invalid order type [" + col[1] + "]");
						}
					}
				}

				pageContext.setVariable(name, qry);
			}
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

	}

	/**
	 * @throws PageException
	 * 
	 */
	private void doGet() throws PageException {
		if (entry == null) throw new ApplicationException("attribute [entry] is required for tag [registry], when action is [get]");
		if (variable == null) throw new ApplicationException("attribute [variable] is required for tag [registry], when action is [get]");

		try {
			RegistryEntry re = RegistryQuery.getValue(branch, entry, type);
			if (re != null) pageContext.setVariable(variable, re.getValue());
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

}