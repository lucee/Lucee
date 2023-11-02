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
package lucee.runtime.ext.tag;

import lucee.runtime.tag.MissingAttribute;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;

public class TagMetaDataAttrImpl extends MissingAttribute implements TagMetaDataAttr {
	private String description;
	private boolean required;
	private boolean isRuntimeExpressionValue;
	private String defaultValue;

	/**
	 * Constructor of the class
	 * 
	 * @param name
	 * @param required
	 * @param type
	 */
	public TagMetaDataAttrImpl(String name, String[] alias, boolean required, String type, boolean isRuntimeExpressionValue, String defaultValue, String description) {
		this(KeyImpl.getInstance(name), alias, required, type, isRuntimeExpressionValue, defaultValue, description);
	}

	/**
	 * Constructor of the class
	 * 
	 * @param name
	 * @param required
	 * @param type
	 * @param description
	 */
	public TagMetaDataAttrImpl(Collection.Key name, String[] alias, boolean required, String type, boolean isRuntimeExpressionValue, String defaultValue, String description) {
		super(name, type, alias);
		this.required = required;
		this.description = description;
		this.defaultValue = defaultValue;
		this.isRuntimeExpressionValue = isRuntimeExpressionValue;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isRequired() {
		return required;
	}

	@Override
	public boolean isRuntimeExpressionValue() {
		return isRuntimeExpressionValue;
	}

	@Override
	public String getDefaultVaue() {
		return defaultValue;
	}
}