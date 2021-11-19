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

import javax.servlet.jsp.tagext.Tag;

import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.functions.other.GetBaseTagData;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;

/**
 * Allows subtag data to be saved with the base tag. Applies only to custom tags.
 *
 *
 *
 **/
public final class Associate extends TagImpl {

	private static final Key ASSOC_ATTRS = KeyImpl.getInstance("AssocAttribs");

	/** The name of the structure in which the base tag stores subtag data. */
	private Collection.Key datacollection = ASSOC_ATTRS;

	/** The name of the base tag. */
	private String basetag;

	@Override
	public void release() {
		super.release();
		datacollection = ASSOC_ATTRS;
	}

	/**
	 * set the value datacollection The name of the structure in which the base tag stores subtag data.
	 * 
	 * @param datacollection value to set
	 **/
	public void setDatacollection(String datacollection) {
		this.datacollection = KeyImpl.init(datacollection);
	}

	/**
	 * set the value basetag The name of the base tag.
	 * 
	 * @param basetag value to set
	 **/
	public void setBasetag(String basetag) {
		this.basetag = basetag;
	}

	@Override
	public int doStartTag() throws PageException {

		// current
		CFTag current = getCFTag();
		Struct value;
		if (current == null || (value = current.getAttributesScope()) == null) throw new ApplicationException("invalid context, tag is no inside a custom tag");

		// parent
		CFTag parent = GetBaseTagData.getParentCFTag(current.getParent(), basetag, -1);
		if (parent == null) throw new ApplicationException("there is no parent tag with name [" + basetag + "]");

		Struct thisTag = parent.getThis();
		Object obj = thisTag.get(datacollection, null);

		Array array;

		if (obj == null) {
			array = new ArrayImpl(new Object[] { value });
			thisTag.set(datacollection, array);
		}
		else if (Decision.isArray(obj) && (array = Caster.toArray(obj)).getDimension() == 1) {
			array.append(value);
		}
		else {
			array = new ArrayImpl(new Object[] { obj, value });
			thisTag.set(datacollection, array);
		}
		return SKIP_BODY;
	}

	/*
	 * private static CFTag getParentCFTag(Tag srcTag,String trgTagName) { String pureName=trgTagName;
	 * CFTag cfTag; if(StringUtil.startsWithIgnoreCase(pureName,"cf_")) {
	 * pureName=pureName.substring(3); } if(StringUtil.startsWithIgnoreCase(pureName,"cf")) {
	 * pureName=pureName.substring(2); } int count=0; while((srcTag=srcTag.getParent())!=null) {
	 * if(srcTag instanceof CFTag) { if(count++==0)continue; cfTag=(CFTag)srcTag; if(cfTag instanceof
	 * CFTagCore){ CFTagCore tc=(CFTagCore) cfTag; if(tc.getName().equalsIgnoreCase(pureName)) return
	 * cfTag; continue; } if(cfTag.getAppendix().equalsIgnoreCase(pureName)) { return cfTag; } } }
	 * return null; }
	 */

	private CFTag getCFTag() {
		Tag tag = this;
		while ((tag = tag.getParent()) != null) {
			if (tag instanceof CFTag) {
				return (CFTag) tag;
			}
		}
		return null;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

}