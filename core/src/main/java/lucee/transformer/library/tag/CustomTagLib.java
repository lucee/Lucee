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
package lucee.transformer.library.tag;

import java.io.File;
import java.util.Map;

import lucee.commons.collection.MapFactory;
import lucee.runtime.tag.CFImportTag;

/**
 * extends the normal tag library, because Custom Tags has no restrictions by a TLD this Taglib
 * accept everything
 */
public final class CustomTagLib extends TagLib {

	private String textTagLib;
	private TagLib[] taglibs;

	/**
	 * constructor of the class
	 * 
	 * @param textTagLib
	 * @param nameSpace the namespace definition
	 * @param nameSpaceSeperator the seperator beetween namespace and name of the tag
	 */
	public CustomTagLib(String textTagLib, String nameSpace, String nameSpaceSeperator) {
		super(false);
		this.textTagLib = textTagLib;
		setNameSpace(nameSpace);
		setNameSpaceSeperator(nameSpaceSeperator);

	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#getAppendixTag(java.lang.String)
	 */
	@Override
	public TagLibTag getAppendixTag(String name) {

		TagLibTag tlt = new TagLibTag(this);
		tlt.setName("");
		tlt.setAppendix(true);
		tlt.setTagClassDefinition(CFImportTag.class.getName(), null, null);
		tlt.setHandleExceptions(true);
		tlt.setBodyContent("free");
		tlt.setParseBody(false);
		tlt.setDescription("Creates a CFML Custom Tag");
		tlt.setAttributeType(TagLibTag.ATTRIBUTE_TYPE_DYNAMIC);

		TagLibTagAttr tlta = new TagLibTagAttr(tlt);
		tlta.setName("__custom_tag_path");
		tlta.setRequired(true);
		tlta.setRtexpr(true);
		tlta.setType("string");
		tlta.setHidden(true);
		tlta.setDefaultValue(textTagLib);

		tlt.setAttribute(tlta);
		setTag(tlt);

		return tlt;
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#getTag(java.lang.String)
	 */
	@Override
	public TagLibTag getTag(String name) {
		if (taglibs != null) {
			TagLibTag tag = null;
			for (int i = 0; i < taglibs.length; i++) {
				if ((tag = taglibs[i].getTag(name)) != null) return tag;
			}
		}
		return null;
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#getTags()
	 */
	@Override
	public Map getTags() {
		return MapFactory.<String, String>getConcurrentMap();
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#setTag(lucee.transformer.library.tag.TagLibTag)
	 */
	@Override
	public void setTag(TagLibTag tag) {
	}

	public void append(TagLib other) {
		if (other instanceof CustomTagLib) textTagLib += File.pathSeparatorChar + ((CustomTagLib) other).textTagLib;
		else {
			if (taglibs == null) {
				taglibs = new TagLib[] { other };
			}
			else {
				TagLib[] tmp = new TagLib[taglibs.length + 1];
				for (int i = 0; i < taglibs.length; i++) {
					tmp[i] = taglibs[i];
				}
				tmp[taglibs.length] = other;
			}

		}
	}

}