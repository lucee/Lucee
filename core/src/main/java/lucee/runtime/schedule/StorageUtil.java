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

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.security.Credentials;
import lucee.commons.security.CredentialsImpl;
import lucee.runtime.config.Config;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.op.Caster;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.Struct;
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
	 * reads a XML Element Attribute ans cast it to a String
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @return Attribute Value
	 */
	public String toString(Struct data, String name) {
		return Caster.toString(data.get(name, null), "");
	}

	/**
	 * reads a XML Element Attribute ans cast it to a File
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @return Attribute Value
	 */
	public Resource toResource(Config config, Struct el, String attributeName) {
		String attributeValue = Caster.toString(el.get(attributeName, null), null);
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
	public boolean toBoolean(Struct data, String name) {
		return Caster.toBooleanValue(data.get(name, null), false);
	}

	/**
	 * reads a XML Element Attribute ans cast it to a boolean value
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @param defaultValue if attribute doesn't exist return default value
	 * @return Attribute Value
	 */
	public boolean toBoolean(Struct el, String attributeName, boolean defaultValue) {
		String value = Caster.toString(el.get(attributeName, null), null);
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
	public int toInt(Struct el, String attributeName) {
		return Caster.toIntValue(el.get(attributeName, null), Integer.MIN_VALUE);
	}

	public long toLong(Struct data, String name) {
		return Caster.toLongValue(data.get(name, null), Long.MIN_VALUE);
	}

	/**
	 * reads a XML Element Attribute ans cast it to an int value
	 * 
	 * @param el XML Element to read Attribute from it
	 * @param attributeName Name of the Attribute to read
	 * @param defaultValue if attribute doesn't exist return default value
	 * @return Attribute Value
	 */
	public int toInt(Struct el, String attributeName, int defaultValue) {
		String value = Caster.toString(el.get(attributeName, null), null);
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
	public DateTime toDateTime(Config config, Struct el, String attributeName) {
		String str = Caster.toString(el.get(attributeName, null), null);
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
	public DateTime toDateTime(Struct el, String attributeName, DateTime defaultValue) {
		String value = Caster.toString(el.get(attributeName, null), null);
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
	public Date toDate(Config config, Struct el, String attributeName) {
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
	public Date toDate(Struct el, String attributeName, Date defaultValue) {
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
	public Time toTime(Config config, Struct el, String attributeName) {
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
	public Time toTime(Struct el, String attributeName, Time defaultValue) {
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
	public Credentials toCredentials(Struct el, String attributeUser, String attributePassword) {
		String user = Caster.toString(el.get(attributeUser, null), null);
		String pass = Caster.toString(el.get(attributePassword, null), null);
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
	public Credentials toCredentials(Struct el, String attributeUser, String attributePassword, Credentials defaultCredentials) {
		String user = Caster.toString(el.get(attributeUser, null), null);
		String pass = Caster.toString(el.get(attributePassword, null), null);
		if (user == null) return defaultCredentials;
		if (pass == null) pass = "";
		return CredentialsImpl.toCredentials(user, pass);
	}
}