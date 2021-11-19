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
package lucee.runtime.text.xml.struct;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Collection;

/**
 * 
 */
public final class XMLTextStruct extends XMLNodeStruct implements Text {

	private Text text;

	/**
	 * @param text
	 * @param caseSensitive
	 */
	public XMLTextStruct(Text text, boolean caseSensitive) {
		super(text, caseSensitive);
		this.text = text;
	}

	@Override
	public Text splitText(int offset) throws DOMException {
		return text.splitText(offset);
	}

	@Override
	public int getLength() {
		return text.getLength();
	}

	@Override
	public void deleteData(int offset, int count) throws DOMException {
		text.deleteData(offset, count);
	}

	@Override
	public String getData() throws DOMException {
		return text.getData();
	}

	@Override
	public String substringData(int offset, int count) throws DOMException {
		return text.substringData(offset, count);
	}

	@Override
	public void replaceData(int offset, int count, String arg) throws DOMException {
		text.replaceData(offset, count, arg);
	}

	@Override
	public void insertData(int offset, String arg) throws DOMException {
		text.insertData(offset, arg);
	}

	@Override
	public void appendData(String arg) throws DOMException {
		text.appendData(arg);
	}

	@Override
	public void setData(String data) throws DOMException {
		text.setData(data);
	}

	// used only with java 7, do not set @Override
	public boolean isElementContentWhitespace() {
		return text.getNodeValue().trim().length() == 0;
	}

	// used only with java 7, do not set @Override
	public String getWholeText() {
		return text.getNodeValue();
	}

	// used only with java 7, do not set @Override
	public Text replaceWholeText(String content) throws DOMException {
		Text oldText = text;
		Document doc = XMLUtil.getDocument(text);
		Text newText = doc.createTextNode(content);
		Node parent = oldText.getParentNode();
		parent.replaceChild(XMLCaster.toRawNode(newText), XMLCaster.toRawNode(oldText));
		return oldText;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new XMLTextStruct((Text) text.cloneNode(deepCopy), caseSensitive);
	}

	@Override
	public Node cloneNode(boolean deep) {
		return new XMLTextStruct((Text) text.cloneNode(deep), caseSensitive);
	}

}