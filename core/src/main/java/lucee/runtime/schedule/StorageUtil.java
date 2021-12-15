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
package lucee.runtime.schedule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.security.Credentials;
import lucee.commons.security.CredentialsImpl;
import lucee.runtime.config.Config;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.type.dt.Date;
import lucee.runtime.type.dt.DateImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.Time;
import lucee.runtime.type.dt.TimeImpl;

/**
 * 
 */
public final class StorageUtil {

	/**
	 * create xml file from a resource definition
	 * 
	 * @param file
	 * @param resourcePath
	 * @throws IOException
	 */
	public void loadFile(File file, String resourcePath) throws IOException {
		loadFile(ResourceUtil.toResource(file), resourcePath);
	}

	/**
	 * create xml file from a resource definition
	 * 
	 * @param res
	 * @param resourcePath
	 * @throws IOException
	 */
	public void loadFile(Resource res, String resourcePath) throws IOException {
		res.createFile(true);
		InputStream is = InfoImpl.class.getResourceAsStream(resourcePath);
		IOUtil.copy(is, res, true);
	}

	/**
	 * return XML Element matching name
	 * 
	 * @param list source node list
	 * @param key key to compare
	 * @param value value to compare
	 * @return matching XML Element
	 */
	public Element getElement(NodeList list, String key, String value) {
		int len = list.getLength();
		for (int i = 0; i < len; i++) {
			Node n = list.item(i);
			if (n instanceof Element) {
				Element el = (Element) n;
				if (el.getAttribute(key).equalsIgnoreCase(value)) return el;
			}
		}
		return null;
	}

	/**
	 * store loaded data to xml file
	 * 
	 * @param doc
	 * @param file
	 * @throws IOException
	 */
	public void store(Document doc, File file) throws IOException {
		store(doc, ResourceUtil.toResource(file));
	}

	/**
	 * store loaded data to xml file
	 * 
	 * @param doc
	 * @param res
	 * @throws IOException
	 */
	public void store(Document doc, Resource res) throws IOException {
		try {
			XMLCaster.writeTo(doc, res);
		}
		catch (PageException e) {
			throw ExceptionUtil.toIOException(e);
		}

		/*
		 * OutputFormat format = new OutputFormat(doc, null, true); format.setLineSeparator("\r\n");
		 * format.setLineWidth(72);
		 * 
		 * OutputStream os=null; try { XMLSerializer serializer = new
		 * XMLSerializer(os=res.getOutputStream(), format); serializer.serialize(doc.getDocumentElement());
		 * } finally { IOUtil.closeEL(os); }
		 */
	}

	/**
	 * reads a XML Element Attribute ans cast it to a String
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @return Attribute Value
	 */
	public String toString(Element el, String attributeName) {
		return el.getAttribute(attributeName);
	}

	/**
	 * reads a XML Element Attribute ans cast it to a String
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @param defaultValue if attribute doesn't exist return default value
	 * @return Attribute Value
	 */
	public String toString(Element el, String attributeName, String defaultValue) {
		String value = el.getAttribute(attributeName);
		return (value == null) ? defaultValue : value;
	}

	/**
	 * reads a XML Element Attribute ans cast it to a File
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @return Attribute Value
	 */
	public Resource toResource(Config config, Element el, String attributeName) {
		String attributeValue = el.getAttribute(attributeName);
		if (attributeValue == null || attributeValue.trim().length() == 0) return null;
		return config.getResource(attributeValue);
	}

	/**
	 * reads a XML Element Attribute ans cast it to a boolean value
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @return Attribute Value
	 */
	public boolean toBoolean(Element el, String attributeName) {
		return Caster.toBooleanValue(el.getAttribute(attributeName), false);
	}

	/**
	 * reads a XML Element Attribute ans cast it to a boolean value
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @param defaultValue if attribute doesn't exist return default value
	 * @return Attribute Value
	 */
	public boolean toBoolean(Element el, String attributeName, boolean defaultValue) {
		String value = el.getAttribute(attributeName);
		if (value == null) return defaultValue;
		return Caster.toBooleanValue(value, false);
	}

	/**
	 * reads a XML Element Attribute ans cast it to an int value
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @return Attribute Value
	 */
	public int toInt(Element el, String attributeName) {
		return Caster.toIntValue(el.getAttribute(attributeName), Integer.MIN_VALUE);
	}

	public long toLong(Element el, String attributeName) {
		return Caster.toLongValue(el.getAttribute(attributeName), Long.MIN_VALUE);
	}

	/**
	 * reads a XML Element Attribute ans cast it to an int value
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @param defaultValue if attribute doesn't exist return default value
	 * @return Attribute Value
	 */
	public int toInt(Element el, String attributeName, int defaultValue) {
		String value = el.getAttribute(attributeName);
		if (value == null) return defaultValue;
		int intValue = Caster.toIntValue(value, Integer.MIN_VALUE);
		if (intValue == Integer.MIN_VALUE) return defaultValue;
		return intValue;
	}

	/**
	 * reads a XML Element Attribute ans cast it to a DateTime Object
	 * 
	 * @param config
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @return Attribute Value
	 */
	public DateTime toDateTime(Config config, Element el, String attributeName) {
		String str = el.getAttribute(attributeName);
		if (str == null) return null;
		return DateCaster.toDateAdvanced(str, ThreadLocalPageContext.getTimeZone(config), null);
	}

	/**
	 * reads a XML Element Attribute ans cast it to a DateTime
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @param defaultValue if attribute doesn't exist return default value
	 * @return Attribute Value
	 */
	public DateTime toDateTime(Element el, String attributeName, DateTime defaultValue) {

		String value = el.getAttribute(attributeName);
		if (value == null) return defaultValue;
		DateTime dtValue = Caster.toDate(value, false, null, null);
		if (dtValue == null) return defaultValue;
		return dtValue;
	}

	/**
	 * reads a XML Element Attribute ans cast it to a Date Object
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @return Attribute Value
	 */
	public Date toDate(Config config, Element el, String attributeName) {
		DateTime dt = toDateTime(config, el, attributeName);
		if (dt == null) return null;
		return new DateImpl(dt);
	}

	/**
	 * reads a XML Element Attribute ans cast it to a Date
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @param defaultValue if attribute doesn't exist return default value
	 * @return Attribute Value
	 */
	public Date toDate(Element el, String attributeName, Date defaultValue) {
		return new DateImpl(toDateTime(el, attributeName, defaultValue));
	}

	/**
	 * reads a XML Element Attribute ans cast it to a Time Object
	 * 
	 * @param config
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @return Attribute Value
	 */
	public Time toTime(Config config, Element el, String attributeName) {
		DateTime dt = toDateTime(config, el, attributeName);
		if (dt == null) return null;
		return new TimeImpl(dt);
	}

	/**
	 * reads a XML Element Attribute ans cast it to a Date
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @param defaultValue if attribute doesn't exist return default value
	 * @return Attribute Value
	 */
	public Time toTime(Element el, String attributeName, Time defaultValue) {
		return new TimeImpl(toDateTime(el, attributeName, defaultValue));
	}

	/**
	 * reads 2 XML Element Attribute ans cast it to a Credential
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeUser Name of the user Attribute to read
	 * @param attributePassword Name of the password Attribute to read
	 * @return Attribute Value
	 */
	public Credentials toCredentials(Element el, String attributeUser, String attributePassword) {
		String user = el.getAttribute(attributeUser);
		String pass = el.getAttribute(attributePassword);
		if (user == null) return null;
		if (pass == null) pass = "";
		return CredentialsImpl.toCredentials(user, pass);
	}

	/**
	 * reads 2 XML Element Attribute ans cast it to a Credential
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeUser Name of the user Attribute to read
	 * @param attributePassword Name of the password Attribute to read
	 * @param defaultCredentials
	 * @return Attribute Value
	 */
	public Credentials toCredentials(Element el, String attributeUser, String attributePassword, Credentials defaultCredentials) {
		String user = el.getAttribute(attributeUser);
		String pass = el.getAttribute(attributePassword);
		if (user == null) return defaultCredentials;
		if (pass == null) pass = "";
		return CredentialsImpl.toCredentials(user, pass);
	}

	/**
	 * sets a string value to a XML Element
	 * 
	 * @param el Element to set value on it
	 * @param key key to set
	 * @param value value to set
	 */
	public void setString(Element el, String key, String value) {
		if (value != null) el.setAttribute(key, value);
	}

	/**
	 * sets a file value to a XML Element
	 * 
	 * @param el Element to set value on it
	 * @param key key to set
	 * @param value value to set
	 */
	public void setFile(Element el, String key, File value) {
		setFile(el, key, ResourceUtil.toResource(value));
	}

	/**
	 * sets a file value to a XML Element
	 * 
	 * @param el Element to set value on it
	 * @param key key to set
	 * @param value value to set
	 */
	public void setFile(Element el, String key, Resource value) {
		if (value != null && value.toString().length() > 0) el.setAttribute(key, value.getAbsolutePath());
	}

	/**
	 * sets a boolean value to a XML Element
	 * 
	 * @param el Element to set value on it
	 * @param key key to set
	 * @param value value to set
	 */
	public void setBoolean(Element el, String key, boolean value) {
		el.setAttribute(key, String.valueOf(value));
	}

	/**
	 * sets an int value to a XML Element
	 * 
	 * @param el Element to set value on it
	 * @param key key to set
	 * @param value value to set
	 */
	public void setInt(Element el, String key, int value) {
		el.setAttribute(key, String.valueOf(value));
	}

	/**
	 * sets a datetime value to a XML Element
	 * 
	 * @param el Element to set value on it
	 * @param key key to set
	 * @param value value to set
	 */
	public void setDateTime(Element el, String key, DateTime value) {
		if (value != null) {
			String str = value.castToString(null);
			if (str != null) el.setAttribute(key, str);
		}
	}

	/**
	 * sets a Credentials to a XML Element
	 * 
	 * @param el
	 * @param username
	 * @param password
	 * @param credentials
	 */
	public void setCredentials(Element el, String username, String password, Credentials c) {
		if (c == null) return;
		if (c.getUsername() != null) el.setAttribute(username, c.getUsername());
		if (c.getPassword() != null) el.setAttribute(password, c.getPassword());
	}
}