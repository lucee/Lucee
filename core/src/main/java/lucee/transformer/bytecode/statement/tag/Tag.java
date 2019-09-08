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
package lucee.transformer.bytecode.statement.tag;

import java.util.Map;

import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.statement.HasBody;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.library.tag.TagLibTagAttr;

public interface Tag extends Statement, HasBody {

	/**
	 * appendix of the tag
	 * 
	 * @return appendix
	 */
	public abstract String getAppendix();

	/**
	 * return all Attributes as a map
	 * 
	 * @return attributes
	 */
	public abstract Map<String, Attribute> getAttributes();

	/**
	 * returns the fullname of the tag
	 * 
	 * @return fullname
	 */
	public abstract String getFullname();

	/**
	 * return the TagLibTag to this tag
	 * 
	 * @return taglibtag
	 */
	public abstract TagLibTag getTagLibTag();

	/**
	 * sets the appendix of the tag
	 * 
	 * @param appendix
	 */
	public abstract void setAppendix(String appendix);

	/**
	 * sets the fullname of the tag
	 * 
	 * @param fullname
	 */
	public abstract void setFullname(String fullname);

	/**
	 * sets the tagLibTag of this tag
	 * 
	 * @param tagLibTag
	 */
	public abstract void setTagLibTag(TagLibTag tagLibTag);

	/**
	 * adds an attribute to the tag
	 * 
	 * @param attribute
	 */
	public abstract void addAttribute(Attribute attribute);

	/**
	 * check if tag has a tag with given name
	 * 
	 * @param name
	 * @return contains attribute
	 */
	public abstract boolean containsAttribute(String name);

	/**
	 * returns the body of the tag
	 * 
	 * @return body of the tag
	 */
	@Override
	public Body getBody();

	/**
	 * sets the body of the tag
	 * 
	 * @param body
	 */
	public abstract void setBody(Body body);

	/**
	 * returns a specified attribute from the tag
	 * 
	 * @param name
	 * @return
	 */
	public abstract Attribute getAttribute(String name);

	/**
	 * returns a specified attribute from the tag
	 * 
	 * @param name
	 * @return
	 */
	public abstract Attribute removeAttribute(String name);

	public abstract void addMissingAttribute(TagLibTagAttr attr);

	public abstract TagLibTagAttr[] getMissingAttributes();

	public abstract void setScriptBase(boolean scriptBase);

	public abstract boolean isScriptBase();

	// public abstract void setHint(String hint);
	public abstract void addMetaData(Attribute metadata);

	// public abstract String getHint();
	public abstract Map<String, Attribute> getMetaData();
}