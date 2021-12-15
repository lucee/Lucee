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

import java.lang.reflect.Method;

import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.util.ArrayUtil;

/**
 * 
 */
public final class XMLCDATASectionStruct extends XMLNodeStruct implements CDATASection {

	private CDATASection section;

	/**
	 * constructor of the class
	 * 
	 * @param section
	 * @param caseSensitive
	 */
	public XMLCDATASectionStruct(CDATASection section, boolean caseSensitive) {
		super(section, caseSensitive);
		this.section = section;
	}

	@Override
	public Text splitText(int offset) throws DOMException {
		return section.splitText(offset);
	}

	@Override
	public int getLength() {
		return section.getLength();
	}

	@Override
	public void deleteData(int offset, int count) throws DOMException {
		section.deleteData(offset, count);
	}

	@Override
	public String getData() throws DOMException {
		return section.getData();
	}

	@Override
	public String substringData(int offset, int count) throws DOMException {
		return section.substringData(offset, count);
	}

	@Override
	public void replaceData(int offset, int count, String arg) throws DOMException {
		section.replaceData(offset, count, arg);
	}

	@Override
	public void insertData(int offset, String arg) throws DOMException {
		section.insertData(offset, arg);
	}

	@Override
	public void appendData(String arg) throws DOMException {
		section.appendData(arg);
	}

	@Override
	public void setData(String data) throws DOMException {
		section.setData(data);
	}

	// used only with java 7, do not set @Override
	public String getWholeText() {
		// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = section.getClass().getMethod("getWholeText", new Class[] {});
			return Caster.toString(m.invoke(section, ArrayUtil.OBJECT_EMPTY));
		}
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	public boolean isElementContentWhitespace() {
		// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = section.getClass().getMethod("isElementContentWhitespace", new Class[] {});
			return Caster.toBooleanValue(m.invoke(section, ArrayUtil.OBJECT_EMPTY));
		}
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	// used only with java 7, do not set @Override
	public Text replaceWholeText(String arg0) throws DOMException {
		// dynamic load to support jre 1.4 and 1.5
		try {
			Method m = section.getClass().getMethod("replaceWholeText", new Class[] { arg0.getClass() });
			return (Text) m.invoke(section, new Object[] { arg0 });
		}
		catch (Exception e) {
			throw new PageRuntimeException(Caster.toPageException(e));
		}
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new XMLCDATASectionStruct((CDATASection) section.cloneNode(deepCopy), caseSensitive);
	}

	@Override
	public Node cloneNode(boolean deep) {
		return new XMLCDATASectionStruct((CDATASection) section.cloneNode(deep), caseSensitive);
	}

}