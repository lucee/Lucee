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

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;

import lucee.commons.lang.ClassException;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.net.ldap.LDAPClient;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Query;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ListUtil;

// TODO tag ldap 
// attr rebind

/**
 * Provides an interface to LDAP Lightweight Directory Access Protocol directory servers like the
 * Netscape Directory Server.
 */
public final class Ldap extends TagImpl {

	private String delimiter = ";";
	private String server;
	private int port = 389;
	private short secureLevel = LDAPClient.SECURE_NONE;
	private String[] returnAsBinary = new String[0];
	private String attributes = null;
	private String username;
	private String password;
	private String action = "query";
	private String[] sort = new String[0];
	private String dn;
	private int referral;
	private int scope = SearchControls.SUBTREE_SCOPE;

	private int sortType = LDAPClient.SORT_TYPE_CASE;
	private int sortDirection = LDAPClient.SORT_DIRECTION_ASC;

	private int startrow = 1;
	private int timeout = 60000;
	private int maxrows;
	private String name;
	private String start;
	private String separator = ",";
	private String filter = "objectclass = *";
	private int modifyType = DirContext.REPLACE_ATTRIBUTE;
	private boolean rebind;

	@Override
	public void release() {
		action = "query";
		delimiter = ";";
		port = 389;
		secureLevel = LDAPClient.SECURE_NONE;
		returnAsBinary = new String[0];
		username = null;
		password = null;
		referral = 0;
		attributes = null;
		sort = new String[0];
		dn = null;
		name = null;
		scope = SearchControls.SUBTREE_SCOPE;

		startrow = 1;
		timeout = 60000;
		maxrows = -1;

		sortType = LDAPClient.SORT_TYPE_CASE;
		sortDirection = LDAPClient.SORT_DIRECTION_ASC;

		start = null;
		separator = ",";
		filter = "objectclass = *";
		modifyType = DirContext.REPLACE_ATTRIBUTE;
		rebind = false;

		super.release();

	}

	/**
	 * @param filterfile The filterfile to set.
	 * @throws ApplicationException
	 */
	public void setFilterfile(String filterfile) {
		// DeprecatedUtil.tagAttribute(pageContext,"LDAP", "filterfile");
	}

	/**
	 * Specifies the character that cfldap uses to separate multiple attribute name/value pairs when
	 * more than one attribute is specified in the attribute attribute or the attribute that you want to
	 * use has the default delimiter character, which is the semicolon (;), such as
	 * mgrpmsgrejecttext;lang-en. The delimiter character is used by the query, add, and modify action
	 * attributes, and is used by cfldap to output multi-value attributes
	 * 
	 * @param delimiter delimiter to set
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Used in conjunction with action = "Query". Specifies the first row of the LDAP query to insert
	 * into the query. The default is 1.
	 * 
	 * @param startrow The startrow to set.
	 */
	public void setStartrow(double startrow) {
		this.startrow = (int) startrow;
	}

	/**
	 * Specifies the maximum number of entries for LDAP queries.
	 * 
	 * @param maxrows The maxrows to set.
	 */
	public void setMaxrows(double maxrows) {
		this.maxrows = (int) maxrows;
	}

	/**
	 * Specifies the maximum amount of time, in milliseconds, to wait for LDAP processing. Defaults to 60
	 * seconds.
	 * 
	 * @param timeout The timeout to set.
	 */
	public void setTimeout(double timeout) {
		this.timeout = (int) timeout;
	}

	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Port defaults to the standard LDAP port, 389.
	 * 
	 * @param port The port to set.
	 */
	public void setPort(double port) {
		this.port = (int) port;
	}

	/**
	 * Identifies the type of security to employ, CFSSL_BASIC or CFSSL_CLIENT_AUTH, and additional
	 * information that is required by the specified security type.
	 * 
	 * @param referral The referral to set.
	 */
	public void setReferral(double referral) {
		this.referral = (int) referral;
	}

	/**
	 * Host name "biff.upperlip.com" or IP address "192.1.2.225" of the LDAP server.
	 * 
	 * @param server The server to set.
	 */
	public void setServer(String server) {
		this.server = server;
	}

	/**
	 * If no user name is specified, the LDAP connection is anonymous.
	 * 
	 * @param username The username to set.
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param secure The secureLevel to set.
	 * @throws ApplicationException
	 */
	public void setSecure(String secure) throws ApplicationException {
		secure = secure.trim().toUpperCase();
		if (secure.equals("CFSSL_BASIC")) secureLevel = LDAPClient.SECURE_CFSSL_BASIC;
		else if (secure.equals("CFSSL_CLIENT_AUTH")) secureLevel = LDAPClient.SECURE_CFSSL_CLIENT_AUTH;
		else throw new ApplicationException("invalid value for attribute secure [" + secure + "], valid values are [CFSSL_BASIC, CFSSL_CLIENT_AUTH]");
	}

	/**
	 * Specifies the scope of the search from the entry specified in the Start attribute for action =
	 * "Query".
	 * 
	 * @param strScope The scope to set.
	 * @throws ApplicationException
	 */
	public void setScope(String strScope) throws ApplicationException {
		strScope = strScope.trim().toLowerCase();
		if (strScope.equals("onelevel")) scope = SearchControls.ONELEVEL_SCOPE;
		else if (strScope.equals("base")) scope = SearchControls.OBJECT_SCOPE;
		else if (strScope.equals("subtree")) scope = SearchControls.SUBTREE_SCOPE;
		else throw new ApplicationException("invalid value for attribute scope [" + strScope + "], valid values are [oneLevel,base,subtree]");
	}

	/**
	 * Indicates whether to add, delete, or replace an attribute in a multi-value list of attributes.
	 * 
	 * @param modifyType The modifyType to set.
	 * @throws ApplicationException
	 */
	public void setModifytype(String modifyType) throws ApplicationException {
		modifyType = modifyType.trim().toLowerCase();
		if (modifyType.equals("add")) this.modifyType = DirContext.ADD_ATTRIBUTE;
		else if (modifyType.equals("delete")) this.modifyType = DirContext.REMOVE_ATTRIBUTE;
		else if (modifyType.equals("replace")) this.modifyType = DirContext.REPLACE_ATTRIBUTE;
		else throw new ApplicationException("invalid value for attribute modifyType [" + modifyType + "], valid values are [add,replace,delete]");
	}

	/**
	 * @param returnAsBinary The returnAsBinary to set.
	 * @throws PageException
	 */
	public void setReturnasbinary(String returnAsBinary) throws PageException {
		this.returnAsBinary = ArrayUtil.trimItems(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(returnAsBinary, ',')));
	}

	/**
	 * Indicates the attribute or attributes by which to sort query results. Use a comma [,] to separate
	 * attributes.
	 * 
	 * @param sort The sort to set.
	 * @throws PageException
	 */
	public void setSort(String sort) throws PageException {
		this.sort = ArrayUtil.trimItems(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(sort, ',')));
	}

	/**
	 * Specifies how to sort query results.
	 * 
	 * @param sortControl sortControl to set
	 * @throws PageException
	 */
	public void setSortcontrol(String sortControl) throws PageException {
		String[] sortControlArr = ArrayUtil.trimItems(ListUtil.toStringArray(ListUtil.listToArrayRemoveEmpty(sortControl, ',')));
		for (int i = 0; i < sortControlArr.length; i++) {
			String scs = sortControlArr[i].trim().toLowerCase();

			if (scs.equals("asc")) sortDirection = LDAPClient.SORT_DIRECTION_ASC;
			else if (scs.equals("desc")) sortDirection = LDAPClient.SORT_DIRECTION_DESC;
			else if (scs.equals("case")) sortType = LDAPClient.SORT_TYPE_CASE;
			else if (scs.equals("nocase")) sortType = LDAPClient.SORT_TYPE_NOCASE;
			else throw new ApplicationException("invalid value for attribute sortControl [" + sortControl + "], " + "valid values are [asc,desc,case,nocase]");
		}
	}

	/**
	 * @param strAttributes
	 */
	public void setAttributes(String strAttributes) {
		attributes = strAttributes;
	}

	/**
	 * Specifies the LDAP action.
	 * 
	 * @param action The action to set.
	 */
	public void setAction(String action) {
		this.action = action.trim().toLowerCase();
	}

	/**
	 * Specifies the distinguished name for update actions.
	 * 
	 * @param dn The dn to set.
	 */
	public void setDn(String dn) {
		this.dn = dn;
	}

	/**
	 * The name you assign to the LDAP query.
	 * 
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Specifies the character that cfldap uses to separate attribute values in multi-value attributes.
	 * This character is used by the query, add, and modify action attributes, and by cfldap to output
	 * multi-value attributes. The default character is the comma (,).
	 * 
	 * @param separator The separator to set.
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * Specifies the distinguished name of the entry to be used to start the search.
	 * 
	 * @param start The start to set.
	 */
	public void setStart(String start) {
		this.start = start;
	}

	/**
	 * @param filter The filter to set.
	 */
	public void setFilter(String filter) {
		this.filter = filter;
	}

	/**
	 * If you set rebind to Yes, cfldap attempts to rebind the referral callback and reissue the query
	 * by the referred address using the original credentials. The default is No, which means referred
	 * connections are anonymous.
	 * 
	 * @param rebind The rebind to set.
	 */
	public void setRebind(boolean rebind) {
		this.rebind = rebind;
	}

	@Override
	public int doStartTag() throws PageException {
		try {
			return _doStartTag();
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private int _doStartTag() throws NamingException, PageException, IOException, ClassException {

		// LDAPClient client=new
		// LDAPClient(server,port,secureLevel,returnAsBinary,username,password,referral);
		LDAPClient client = new LDAPClient(server, port, timeout, returnAsBinary);
		if (secureLevel != LDAPClient.SECURE_NONE) client.setSecureLevel(secureLevel);
		if (username != null) client.setCredential(username, password);
		if (referral > 0) client.setReferral(referral);

		if (action.equals("add")) {
			required("LDAP", action, "attributes", attributes);
			required("LDAP", action, "dn", dn);
			client.add(dn, attributes, delimiter, separator);
		}
		else if (action.equals("delete")) {
			required("LDAP", action, "dn", dn);
			client.delete(dn);
		}
		else if (action.equals("modifydn")) {
			required("LDAP", action, "attributes", attributes);
			required("LDAP", action, "dn", dn);
			client.modifydn(dn, attributes);
		}
		else if (action.equals("modify")) {
			required("LDAP", action, "attributes", attributes);
			required("LDAP", action, "dn", dn);
			client.modify(dn, modifyType, attributes, delimiter, separator);
		}
		else if (action.equals("query")) {
			required("LDAP", action, "start", start);
			required("LDAP", action, "attributes", attributes);
			required("LDAP", action, "name", name);
			Query qry = client.query(attributes, scope, startrow, maxrows, timeout, sort, sortType, sortDirection, start, separator, filter);
			pageContext.setVariable(name, qry);

		}
		else throw new ApplicationException("invalid value for attribute action [" + action + "], valid values are [add,delete,modifydn,modify,query]");

		return SKIP_BODY;
	}

}