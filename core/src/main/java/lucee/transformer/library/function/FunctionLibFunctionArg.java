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
package lucee.transformer.library.function;

import java.io.IOException;

import lucee.loader.util.VersionImpl;
import lucee.runtime.config.maven.Version;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.Md5;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.osgi.OSGiUtil;
import lucee.transformer.library.tag.TagLib;

/**
 * Eine FunctionLibFunctionArg repraesentiert ein einzelnes Argument einer Funktion.
 */
public final class FunctionLibFunctionArg {

	private static final short UNDEFINED = -12553;

	private String strType;
	private boolean required;
	private FunctionLibFunction function;
	private String name;
	private String nameWithCase;
	private String description = "";
	private String alias = null;
	private String[] aliases = null;
	private String defaultValue = null;
	private boolean hidden;
	private short status = TagLib.STATUS_IMPLEMENTED;
	private short type = UNDEFINED;
	private Version introduced;

	/**
	 * Geschuetzer Konstruktor ohne Argumente.
	 */
	public FunctionLibFunctionArg() {
	}

	public FunctionLibFunctionArg(FunctionLibFunction function) {
		this.function = function;
	}

	public FunctionLibFunctionArg duplicate(FunctionLibFunction function) {
		FunctionLibFunctionArg dbl = new FunctionLibFunctionArg(function);

		dbl.strType = strType;
		dbl.required = required;
		dbl.name = name;
		dbl.nameWithCase = nameWithCase;
		dbl.description = description;
		dbl.alias = alias;
		dbl.aliases = aliases;
		dbl.defaultValue = defaultValue;
		dbl.hidden = hidden;
		dbl.status = status;
		dbl.type = type;
		dbl.introduced = introduced;
		return dbl;
	}

	/**
	 * Gibt den Typ des Argument als String zurueck (query, struct, string usw.)
	 * 
	 * @return Typ des Argument
	 */
	public String getTypeAsString() {
		return this.strType;
	}

	/**
	 * Gibt den Typ des Argument zurueck (query, struct, string usw.)
	 * 
	 * @return Typ des Argument
	 */
	public short getType() {
		if (type == UNDEFINED) {
			type = CFTypes.toShort(strType, false, CFTypes.TYPE_UNKNOW);
		}
		return type;
	}

	/**
	 * @return the status
	 *         (TagLib.,TagLib.STATUS_IMPLEMENTED,TagLib.STATUS_DEPRECATED,TagLib.STATUS_UNIMPLEMENTED)
	 */
	public short getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 *            (TagLib.,TagLib.STATUS_IMPLEMENTED,TagLib.STATUS_DEPRECATED,TagLib.STATUS_UNIMPLEMENTED)
	 */
	public void setStatus(short status) {
		this.status = status;
	}

	/**
	 * Gibt zurueck, ob das Argument Pflicht ist oder nicht, alias fuer isRequired.
	 * 
	 * @return Ist das Argument Pflicht.
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * Gibt zurueck, ob das Argument Pflicht ist oder nicht.
	 * 
	 * @return Ist das Argument Pflicht.
	 */
	public boolean getRequired() {
		return required;
	}

	/**
	 * Gibt die Funktion zurueck zu der das Argument gehoert.
	 * 
	 * @return Zugehoerige Funktion.
	 */
	public FunctionLibFunction getFunction() {
		return function;
	}

	/**
	 * Setzt die Funktion zu der das Argument gehoert.
	 * 
	 * @param function Zugehoerige Funktion.
	 */
	protected void setFunction(FunctionLibFunction function) {
		this.function = function;
	}

	/**
	 * Setzt, den Typ des Argument (query, struct, string usw.)
	 * 
	 * @param type Typ des Argument.
	 */
	public void setType(String type) {
		this.strType = type;
	}

	/**
	 * Setzt, ob das Argument Pflicht ist oder nicht.
	 * 
	 * @param value Ist das Argument Pflicht.
	 */
	public void setRequired(String value) {
		value = value.toLowerCase().trim();
		required = (value.equals("yes") || value.equals("true"));
	}

	public void setRequired(boolean value) {
		required = value;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the name, without forcing it to lowercase
	 */
	public String getNameWithCase() {
		return nameWithCase;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
		this.nameWithCase = name;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getHash() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.getDefaultValue());
		sb.append(this.getName());
		sb.append(this.getRequired());
		sb.append(this.getTypeAsString());
		sb.append(this.getTypeAsString());
		sb.append(this.getAlias());

		try {
			return Md5.getDigestAsString(sb.toString());
		}
		catch (IOException e) {
			return "";
		}
	}

	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
		this.aliases = ListUtil.trimItems(ListUtil.listToStringArray(alias.toLowerCase(), ','));
	}

	/**
	 * @return the aliases
	 */
	public String[] getAliases() {
		return aliases;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public void setIntroduced(String introduced) {
		this.introduced = VersionImpl.parse(introduced, true, null);
	}

	public Version getIntroduced() {
		return introduced;
	}

	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}
}