/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */

package lucee.transformer.library.tag;

import java.util.Map;

import org.xml.sax.Attributes;

import lucee.runtime.config.Identification;
import lucee.runtime.db.ClassDefinition;
import lucee.transformer.cfml.ExprTransformer;

/**
 * 
 */
public final class ImportTagLib extends TagLib {

	private String taglib;
	private String prefix;

	public ImportTagLib(String taglib, String prefix) {
		super(false);
		this.taglib = taglib;
		this.prefix = prefix;
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#getAppendixTag(java.lang.String)
	 */
	@Override
	public TagLibTag getAppendixTag(String name) {
		return super.getAppendixTag(name);
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#getELClass()
	 */
	@Override
	public ClassDefinition<? extends ExprTransformer> getELClassDefinition() {
		return super.getELClassDefinition();
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#getExprTransfomer()
	 */
	@Override
	public ExprTransformer getExprTransfomer() throws TagLibException {
		return super.getExprTransfomer();
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#getNameSpace()
	 */
	@Override
	public String getNameSpace() {
		return super.getNameSpace();
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#getNameSpaceAndSeparator()
	 */
	@Override
	public String getNameSpaceAndSeparator() {
		return super.getNameSpaceAndSeparator();
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#getNameSpaceSeparator()
	 */
	@Override
	public String getNameSpaceSeparator() {
		return super.getNameSpaceSeparator();
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#getTag(java.lang.String)
	 */
	@Override
	public TagLibTag getTag(String name) {
		return super.getTag(name);
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#getTags()
	 */
	@Override
	public Map getTags() {
		return super.getTags();
	}

	@Override
	protected void setELClass(String eLClass, Identification id, Attributes attributes) {
		super.setELClass(eLClass, id, attributes);
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#setNameSpace(java.lang.String)
	 */
	@Override
	public void setNameSpace(String nameSpace) {
		super.setNameSpace(nameSpace);
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#setNameSpaceSeperator(java.lang.String)
	 */
	@Override
	public void setNameSpaceSeperator(String nameSpaceSeperator) {
		super.setNameSpaceSeperator(nameSpaceSeperator);
	}

	/**
	 * @see lucee.transformer.library.tag.TagLib#setTag(lucee.transformer.library.tag.TagLibTag)
	 */
	@Override
	public void setTag(TagLibTag tag) {
		super.setTag(tag);
	}
}