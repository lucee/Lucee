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
package lucee.runtime.net.ldap;

import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.util.ListUtil;

/**
 * Ldap Client
 */
public final class LDAPClient {

	/**
	 * Field <code>SECURE_NONE</code>
	 */
	public static final short SECURE_NONE = 0;
	/**
	 * Field <code>SECURE_CFSSL_BASIC</code>
	 */
	public static final short SECURE_CFSSL_BASIC = 1;
	/**
	 * Field <code>SECURE_CFSSL_CLIENT_AUTH</code>
	 */
	public static final short SECURE_CFSSL_CLIENT_AUTH = 2;

	/**
	 * Field <code>SORT_TYPE_CASE</code>
	 */
	public static final int SORT_TYPE_CASE = 0;
	/**
	 * Field <code>SORT_TYPE_NOCASE</code>
	 */
	public static final int SORT_TYPE_NOCASE = 1;

	/**
	 * Field <code>SORT_DIRECTION_ASC</code>
	 */
	public static final int SORT_DIRECTION_ASC = 0;
	/**
	 * Field <code>SORT_DIRECTION_DESC</code>
	 */
	public static final int SORT_DIRECTION_DESC = 1;

	Hashtable env = new Hashtable();

	/**
	 * constructor of the class
	 * 
	 * @param server
	 * @param port
	 * @param binaryColumns
	 */
	public LDAPClient(String server, int port, int timeout, String[] binaryColumns) {

		env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
		env.put("java.naming.provider.url", "ldap://" + server + ":" + port);

		// rEAD AS bINARY
		for (int i = 0; i < binaryColumns.length; i++)
			env.put("java.naming.ldap.attributes.binary", binaryColumns[i]);

		// Referral
		env.put("java.naming.referral", "ignore");

		// timeout
		env.put("com.sun.jndi.ldap.read.timeout", String.valueOf(timeout));
	}

	/**
	 * sets username password for the connection
	 * 
	 * @param username
	 * @param password
	 */
	public void setCredential(String username, String password) {
		if (username != null) {
			env.put("java.naming.security.principal", username);
			env.put("java.naming.security.credentials", password);
		}
		else {
			env.remove("java.naming.security.principal");
			env.remove("java.naming.security.credentials");
		}
	}

	/**
	 * sets the secure Level
	 * 
	 * @param secureLevel [SECURE_CFSSL_BASIC, SECURE_CFSSL_CLIENT_AUTH, SECURE_NONE]
	 * @throws ClassNotFoundException
	 * @throws ClassException
	 */
	public void setSecureLevel(short secureLevel) throws ClassException {
		// Security
		if (secureLevel == SECURE_CFSSL_BASIC) {
			env.put("java.naming.security.protocol", "ssl");
			env.put("java.naming.ldap.factory.socket", "javax.net.ssl.SSLSocketFactory");
			Class clazz = ClassUtil.loadClass("com.sun.net.ssl.internal.ssl.Provider");

			try {
				Security.addProvider((Provider) ClassUtil.newInstance(clazz));
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		else if (secureLevel == SECURE_CFSSL_CLIENT_AUTH) {
			env.put("java.naming.security.protocol", "ssl");
			env.put("java.naming.security.authentication", "EXTERNAL");
		}
		else {
			env.put("java.naming.security.authentication", "simple");
			env.remove("java.naming.security.protocol");
			env.remove("java.naming.ldap.factory.socket");
		}
	}

	/**
	 * sets thr referral
	 * 
	 * @param referral
	 */
	public void setReferral(int referral) {
		if (referral > 0) {
			env.put("java.naming.referral", "follow");
			env.put("java.naming.ldap.referral.limit", Caster.toString(referral));
		}
		else {
			env.put("java.naming.referral", "ignore");
			env.remove("java.naming.ldap.referral.limit");
		}
	}

	/**
	 * adds LDAP entries to LDAP server
	 * 
	 * @param dn
	 * @param attributes
	 * @param delimiter
	 * @throws NamingException
	 * @throws PageException
	 */
	public void add(String dn, String attributes, String delimiter, String seperator) throws NamingException, PageException {
		DirContext ctx = new InitialDirContext(env);
		ctx.createSubcontext(dn, toAttributes(attributes, delimiter, seperator));
		ctx.close();
	}

	/**
	 * deletes LDAP entries on an LDAP server
	 * 
	 * @param dn
	 * @throws NamingException
	 */
	public void delete(String dn) throws NamingException {
		DirContext ctx = new InitialDirContext(env);
		ctx.destroySubcontext(dn);
		ctx.close();
	}

	/**
	 * modifies distinguished name attribute for LDAP entries on LDAP server
	 * 
	 * @param dn
	 * @param attributes
	 * @throws NamingException
	 */
	public void modifydn(String dn, String attributes) throws NamingException {
		DirContext ctx = new InitialDirContext(env);
		ctx.rename(dn, attributes);
		ctx.close();
	}

	public void modify(String dn, int modifytype, String strAttributes, String delimiter, String separator) throws NamingException, PageException {

		DirContext context = new InitialDirContext(env);
		String strArrAttributes[] = toStringAttributes(strAttributes, delimiter);

		int count = 0;
		for (int i = 0; i < strArrAttributes.length; i++) {
			String[] attributesValues = getAttributesValues(strArrAttributes[i], separator);
			if (attributesValues == null) count++;
			else count += attributesValues.length;
		}

		ModificationItem modItems[] = new ModificationItem[count];
		BasicAttribute basicAttr = null;
		int k = 0;
		for (int i = 0; i < strArrAttributes.length; i++) {
			String attribute = strArrAttributes[i];
			String type = getAttrValueType(attribute);
			String values[] = getAttributesValues(attribute, separator);

			if (modifytype == DirContext.REPLACE_ATTRIBUTE) {
				if (values == null) basicAttr = new BasicAttribute(type);
				else basicAttr = new BasicAttribute(type, values[0]);

				modItems[k] = new ModificationItem(modifytype, basicAttr);
				k++;
				if (values != null && values.length > 1) {
					for (int j = 1; j < values.length; j++) {
						basicAttr = new BasicAttribute(type, values[j]);
						modItems[k] = new ModificationItem(DirContext.ADD_ATTRIBUTE, basicAttr);
						k++;
					}
				}
			}
			else {
				for (int j = 0; j < values.length; j++) {
					if (type != null || modifytype == DirContext.ADD_ATTRIBUTE) basicAttr = new BasicAttribute(type, values[j]);
					else basicAttr = new BasicAttribute(values[j]);
					modItems[k] = new ModificationItem(modifytype, basicAttr);
					k++;
				}
			}
		}

		context.modifyAttributes(dn, modItems);
		context.close();

	}

	/**
	 * @param dn
	 * @param strAttributes
	 * @param scope
	 * @param startrow
	 * @param maxrows
	 * @param timeout
	 * @param sort
	 * @param sortType
	 * @param sortDirection
	 * @param start
	 * @param separator
	 * @param filter
	 * @return
	 * @throws NamingException
	 * @throws PageException
	 * @throws IOException
	 */
	public Query query(String strAttributes, int scope, int startrow, int maxrows, int timeout, String[] sort, int sortType, int sortDirection, String start, String separator,
			String filter) throws NamingException, PageException, IOException {
		// strAttributes=strAttributes.trim();
		boolean attEQAsterix = strAttributes.trim().equals("*");
		String[] attributes = attEQAsterix ? new String[] { "name", "value" } : toStringAttributes(strAttributes, ",");

		// Control
		SearchControls controls = new SearchControls();
		controls.setReturningObjFlag(true);
		controls.setSearchScope(scope);
		if (!attEQAsterix) controls.setReturningAttributes(toStringAttributes(strAttributes, ","));
		if (maxrows > 0) controls.setCountLimit(startrow + maxrows + 1);
		if (timeout > 0) controls.setTimeLimit(timeout);

		InitialLdapContext context = new InitialLdapContext(env, null);

		// Search
		Query qry = new QueryImpl(attributes, 0, "query");
		try {
			NamingEnumeration results = context.search(start, filter, controls);

			// Fill result
			int row = 1;
			if (!attEQAsterix) {
				while (results.hasMoreElements()) {
					SearchResult resultRow = (SearchResult) results.next();
					if (row++ < startrow) continue;

					int len = qry.addRow();
					NamingEnumeration rowEnum = resultRow.getAttributes().getAll();
					String dn = resultRow.getNameInNamespace();
					qry.setAtEL("dn", len, dn);
					while (rowEnum.hasMore()) {
						Attribute attr = (Attribute) rowEnum.next();
						Collection.Key key = KeyImpl.init(attr.getID());
						Enumeration values = attr.getAll();
						Object value;
						String existing, strValue;
						while (values.hasMoreElements()) {
							value = values.nextElement();
							strValue = Caster.toString(value, null);
							existing = Caster.toString(qry.getAt(key, len, null), null);
							if (!StringUtil.isEmpty(existing) && !StringUtil.isEmpty(strValue)) {
								value = existing + separator + strValue;
							}
							else if (!StringUtil.isEmpty(existing)) value = existing;

							qry.setAtEL(key, len, value);
						}
					}
					if (maxrows > 0 && len >= maxrows) break;
				}
			}
			else {

				outer: while (results.hasMoreElements()) {
					SearchResult resultRow = (SearchResult) results.next();
					if (row++ < startrow) continue;

					Attributes attributesRow = resultRow.getAttributes();
					NamingEnumeration rowEnum = attributesRow.getIDs();
					while (rowEnum.hasMoreElements()) {
						int len = qry.addRow();
						String name = Caster.toString(rowEnum.next());
						Object value = null;

						try {
							value = attributesRow.get(name).get();
						}
						catch (Exception e) {
						}

						qry.setAtEL("name", len, name);
						qry.setAtEL("value", len, value);
						if (maxrows > 0 && len >= maxrows) break outer;
					}
					qry.setAtEL("name", qry.size(), "dn");
				}
			}
		}
		finally {
			context.close();
		}
		// Sort
		if (sort != null && sort.length > 0) {
			int order = sortDirection == SORT_DIRECTION_ASC ? Query.ORDER_ASC : Query.ORDER_DESC;
			for (int i = sort.length - 1; i >= 0; i--) {
				String item = sort[i];
				if (item.indexOf(' ') != -1) item = ListUtil.first(item, " ", true);
				qry.sort(KeyImpl.getInstance(item), order);
				// keys[i] = new SortKey(item);
			}
		}
		return qry;
	}

	private static String[] toStringAttributes(String strAttributes, String delimiter) throws PageException {
		return ListUtil.toStringArrayTrim(ListUtil.listToArrayRemoveEmpty(strAttributes, delimiter));
	}

	private static Attributes toAttributes(String strAttributes, String delimiter, String separator) throws PageException {
		String[] arrAttr = toStringAttributes(strAttributes, delimiter);

		BasicAttributes attributes = new BasicAttributes();
		for (int i = 0; i < arrAttr.length; i++) {
			String strAttr = arrAttr[i];

			// Type
			int eqIndex = strAttr.indexOf('=');
			Attribute attr = new BasicAttribute((eqIndex != -1) ? strAttr.substring(0, eqIndex).trim() : null);

			// Value
			String strValue = (eqIndex != -1) ? strAttr.substring(eqIndex + 1) : strAttr;
			String[] arrValue = ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(strValue, separator));

			// Fill
			for (int y = 0; y < arrValue.length; y++) {
				attr.add(arrValue[y]);
			}
			attributes.put(attr);
		}
		return attributes;

	}

	private String getAttrValueType(String attribute) {
		int eqIndex = attribute.indexOf("=");
		if (eqIndex != -1) return attribute.substring(0, eqIndex).trim();
		return null;
	}

	private String[] getAttributesValues(String attribute, String separator) throws PageException {
		String strValue = attribute.substring(attribute.indexOf("=") + 1);
		if (strValue.length() == 0) return null;
		return ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(strValue, separator.equals(", ") ? "," : separator));
	}

}