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
package lucee.runtime.ext.tag;

public interface TagMetaData {

	/**
	 * Body is not allowed for this tag
	 */
	public int BODY_CONTENT_EMPTY = 0;

	/**
	 * tag can have a body, but it is not required
	 */
	public int BODY_CONTENT_FREE = 1;

	/**
	 * body is required for this tag
	 */
	public int BODY_CONTENT_MUST = 2;

	/**
	 * tag has a fix defined group of attributes, only this attributes are allowed
	 */
	public int ATTRIBUTE_TYPE_FIX = 4;

	/**
	 * there is no restriction or rules for attributes, tag can have as many as whished
	 */
	public int ATTRIBUTE_TYPE_DYNAMIC = 8;

	/**
	 * tag has a fix set of attributes, but is also free in use additional tags
	 */
	public int ATTRIBUTE_TYPE_MIXED = 16;

	/**
	 * type of the body content
	 * 
	 * @return TagMetaData.BODY_CONTENT_EMPTY,TagMetaData.BODY_CONTENT_FREE, TagMetaData
	 *         .BODY_CONTENT_MUST
	 */
	public int getBodyContent();

	/**
	 * attribute type
	 * 
	 * @return TagMetaData.ATTRIBUTE_TYPE_FIX,TagMetaData.ATTRIBUTE_TYPE_DYNAMIC ,
	 *         TagMetaData.ATTRIBUTE_TYPE_MIXED
	 */
	public int getAttributeType();

	/**
	 * minimal count of attributes needed for tag
	 * 
	 * @return minimal count of attributes
	 */
	public int getAttributeMin();

	/**
	 * maximum count of attributes needed for tag
	 * 
	 * @return maximum count of attributes or -1 for infinity attributes
	 */
	public int getAttributeMax();

	/**
	 * is the body of the tag parsed like inside a cfoutput
	 * 
	 * @return parsed or not
	 */
	public boolean isBodyRuntimeExpressionValue();

	/**
	 * A description of the tag.
	 * 
	 * @return description of the tag
	 */
	public String getDescription();

	/**
	 * get attributes of the tag
	 * @return attributes of the tag
	 */
	public TagMetaDataAttr[] getAttributes();

	/**
	 * has the tag a body
	 * 
	 * @return has a body
	 */
	public boolean hasBody();

	/**
	 * can the tag handle exceptions
	 * 
	 * @return can handle exceptions
	 */
	public boolean handleException();

	/**
	 * has the tag an appendix
	 * 
	 * @return has appendix
	 */
	public boolean hasAppendix();

}