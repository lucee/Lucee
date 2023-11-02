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
package lucee.runtime.net.mail;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.Charset;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.CharSet;
import lucee.runtime.net.smtp.StringDataSource;

/**
 *
 */
public final class MailPart implements Externalizable {

	private static final String NULL = "<<null>>";

	/** IThe MIME media type of the part */
	private boolean isHTML;

	/** Specifies the maximum line length, in characters of the mail text */
	private int wraptext = -1;

	/** The character encoding in which the part text is encoded */
	private CharSet charset;

	private String body;
	private String type;

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(isHTML);
		out.writeInt(wraptext);
		writeString(out, charset.name());
		writeString(out, body);
		writeString(out, type);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		isHTML = in.readBoolean();
		wraptext = in.readInt();
		charset = CharsetUtil.toCharSet(readString(in));
		body = readString(in);
		type = readString(in);
	}

	public static void writeString(ObjectOutput out, String str) throws IOException {
		if (str == null) out.writeObject(NULL);
		else out.writeObject(str);
	}

	public static String readString(ObjectInput in) throws ClassNotFoundException, IOException {
		String str = (String) in.readObject();
		if (str.equals(NULL)) return null;
		return str;
	}

	/**
	 *
	 */
	public void clear() {
		isHTML = false;
		wraptext = -1;
		charset = null;
		body = "null";
		type = null;
	}

	/**
	 *
	 */
	public MailPart() {// needed for deserialize
	}

	/**
	 * @param charset
	 */
	public MailPart(Charset charset) {
		this.charset = CharsetUtil.toCharSet(charset);
	}

	/**
	 * @return Returns the body.
	 */
	public String getBody() {
		return this.wraptext > 0 ? StringDataSource.wrapText(body, this.wraptext) : body;
	}

	/**
	 * @param body The body to set.
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * @return Returns the charset.
	 */
	public Charset getCharset() {
		return CharsetUtil.toCharset(charset);
	}

	public CharSet getCharSet() {
		return charset;
	}

	/**
	 * @param charset The charset to set.
	 */
	public void setCharset(Charset charset) {
		this.charset = CharsetUtil.toCharSet(charset);
	}

	public void setCharSet(CharSet charSet) {
		this.charset = charSet;
	}

	/**
	 * @return Returns the isHTML.
	 */
	public boolean isHTML() {
		return isHTML;
	}

	/**
	 * @param isHTML The type to set.
	 */
	public void isHTML(boolean isHTML) {
		this.isHTML = isHTML;
	}

	/**
	 * @return Returns the wraptext.
	 */
	public int getWraptext() {
		return wraptext;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @param wraptext The wraptext to set.
	 */
	public void setWraptext(int wraptext) {
		this.wraptext = wraptext;
	}

	/**
	 * wrap a single line
	 * 
	 * @param str
	 * @return wraped Line
	 */
	private String wrapLine(String str) {
		int wtl = wraptext;

		if (str.length() <= wtl) return str;

		String sub = str.substring(0, wtl);
		String rest = str.substring(wtl);
		char firstR = rest.charAt(0);
		String ls = System.getProperty("line.separator");

		if (firstR == ' ' || firstR == '\t') return sub + ls + wrapLine(rest.length() > 1 ? rest.substring(1) : "");

		int indexSpace = sub.lastIndexOf(' ');
		int indexTab = sub.lastIndexOf('\t');
		int index = indexSpace <= indexTab ? indexTab : indexSpace;

		if (index == -1) return sub + ls + wrapLine(rest);
		return sub.substring(0, index) + ls + wrapLine(sub.substring(index + 1) + rest);

	}

	@Override
	public String toString() {
		return "lucee.runtime.mail.MailPart(wraptext:" + wraptext + ";type:" + type + ";charset:" + charset + ";body:" + body + ";)";
	}

}