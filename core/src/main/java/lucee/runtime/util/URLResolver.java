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
package lucee.runtime.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.transformer.util.SourceCode;

/**
 * Transform a HTML String, set all relative Pathes inside HTML File to absolute TODO Test this
 *
 */
public final class URLResolver {

	private Tag[] tags = new Tag[] { new Tag("a", "href"), new Tag("link", "href"), new Tag("form", "action"), new Tag("applet", "code"), new Tag("script", "src"),
			new Tag("body", "background"), new Tag("frame", "src"), new Tag("bgsound", "src"), new Tag("img", "src"),

			new Tag("embed", new String[] { "src", "pluginspace" }), new Tag("object", new String[] { "data", "classid", "codebase", "usemap" })

	};

	public void transform(Node node, URL url) throws MalformedURLException {
		Element el;
		if (node.getNodeType() == Node.DOCUMENT_NODE) {
			transform(XMLUtil.getRootElement(node, true), url);
		}
		else if (node.getNodeType() == Node.ELEMENT_NODE) {
			el = (Element) node;
			String[] attr;
			NamedNodeMap map;
			String attrName, value, value2, nodeName = el.getNodeName();
			int len;
			// translate attribute
			for (int i = 0; i < tags.length; i++) {
				if (tags[i].tag.equalsIgnoreCase(nodeName)) {

					attr = tags[i].attributes;
					map = el.getAttributes();
					len = map.getLength();
					for (int y = 0; y < attr.length; y++) {
						for (int z = 0; z < len; z++) {
							attrName = map.item(z).getNodeName();
							if (attrName.equalsIgnoreCase(attr[y])) {
								value = el.getAttribute(attrName);
								value2 = add(url, value);

								if (value != value2) {
									el.setAttribute(attrName, value2);
								}

								break;
							}
						}
					}
				}
			}

			// list children
			NodeList nodes = el.getChildNodes();
			len = nodes.getLength();
			for (int i = 0; i < len; i++) {
				transform(nodes.item(i), url);
			}
		}
	}

	/**
	 * transform the HTML String
	 * 
	 * @param html HTML String to transform
	 * @param url Absolute URL path to set
	 * @return transformed HTMl String
	 * @throws PageException
	 */
	public String transform(String html, URL url, boolean setBaseTag) throws PageException {
		StringBuffer target = new StringBuffer();
		SourceCode cfml = new SourceCode(html, false, CFMLEngine.DIALECT_CFML);
		while (!cfml.isAfterLast()) {
			if (cfml.forwardIfCurrent('<')) {
				target.append('<');
				try {
					for (int i = 0; i < tags.length; i++) {
						if (cfml.forwardIfCurrent(tags[i].tag + " ")) {
							target.append(tags[i].tag + " ");
							transformTag(target, cfml, tags[i], url);
						}
					}
				}
				catch (MalformedURLException me) {
					throw Caster.toPageException(me);
				}
			}
			else {
				target.append(cfml.getCurrent());
				cfml.next();
			}

		}
		if (!setBaseTag) return target.toString();

		html = target.toString();
		String prefix = "", postfix = "";
		int index = StringUtil.indexOfIgnoreCase(html, "</head>");
		if (index == -1) {
			prefix = "<head>";
			postfix = "</head>";
			index = StringUtil.indexOfIgnoreCase(html, "</html>");
		}

		if (index != -1) {
			StringBuffer sb = new StringBuffer();
			sb.append(html.substring(0, index));
			String port = url.getPort() == -1 ? "" : ":" + url.getPort();
			sb.append(prefix + "<base href=\"" + (url.getProtocol() + "://" + url.getHost() + port) + "\">" + postfix);
			sb.append(html.substring(index));
			html = sb.toString();

		}
		return html;
	}

	/**
	 * transform a single tag
	 * 
	 * @param target target to write to
	 * @param cfml CFMl String Object containing plain HTML
	 * @param tag current tag totransform
	 * @param url absolute URL to Set at tag attribute
	 * @throws MalformedURLException
	 */
	private void transformTag(StringBuffer target, SourceCode cfml, Tag tag, URL url) throws MalformedURLException {
		// TODO attribute inside other attribute

		char quote = 0;
		boolean inside = false;
		StringBuffer value = new StringBuffer();

		while (!cfml.isAfterLast()) {
			if (inside) {
				if (quote != 0 && cfml.forwardIfCurrent(quote)) {
					inside = false;
					target.append(add(url, value.toString()));
					target.append(quote);
				}
				else if (quote == 0 && (cfml.isCurrent(' ') || cfml.isCurrent("/>") || cfml.isCurrent('>') || cfml.isCurrent('\t') || cfml.isCurrent('\n'))) {

					inside = false;
					target.append(new URL(url, value.toString()));
					target.append(cfml.getCurrent());
					cfml.next();
				}
				else {
					value.append(cfml.getCurrent());
					cfml.next();
				}
			}
			else if (cfml.forwardIfCurrent('>')) {
				target.append('>');
				break;
			}
			else {

				for (int i = 0; i < tag.attributes.length; i++) {
					if (cfml.forwardIfCurrent(tag.attributes[i])) {
						target.append(tag.attributes[i]);
						cfml.removeSpace();
						// =
						if (cfml.isCurrent('=')) {
							inside = true;
							target.append('=');
							cfml.next();
							cfml.removeSpace();

							quote = cfml.getCurrent();
							value = new StringBuffer();
							if (quote != '"' && quote != '\'') quote = 0;
							else {
								target.append(quote);
								cfml.next();
							}
						}
					}
				}
				if (!inside) {
					target.append(cfml.getCurrent());
					cfml.next();
				}
			}
		}
	}

	private String add(URL url, String value) {
		value = value.trim();
		String lcValue = value.toLowerCase();
		if (lcValue.startsWith("http://") || lcValue.startsWith("file://") || lcValue.startsWith("news://") || lcValue.startsWith("goopher://")
				|| lcValue.startsWith("javascript:"))
			return (value);
		try {
			return new URL(url, value.toString()).toExternalForm();
		}
		catch (MalformedURLException e) {
			return value;
		}
	}

	private class Tag {
		private String tag;
		private String[] attributes;

		private Tag(String tag, String[] attributes) {
			this.tag = tag.toLowerCase();
			this.attributes = new String[attributes.length];
			for (int i = 0; i < attributes.length; i++) {
				this.attributes[i] = attributes[i].toLowerCase();
			}

		}

		private Tag(String tag, String attribute1) {
			this.tag = tag.toLowerCase();
			this.attributes = new String[] { attribute1.toLowerCase() };
		}

	}

	public static URLResolver getInstance() {
		return new URLResolver();
	}

}