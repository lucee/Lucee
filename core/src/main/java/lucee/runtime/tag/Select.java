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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.jsp.tagext.Tag;

import lucee.commons.lang.HTMLEntities;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.OpUtil;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

/**
 * 
 */
public final class Select extends BodyTagImpl {

	private static final int QUERY_POSITION_ABOVE = 0;
	private static final int QUERY_POSITION_BELOW = 1;
	private lucee.runtime.type.Query query;
	private String[] selected;
	private String value;
	private String display;
	private String passthrough;

	private Struct attributes = new StructImpl();
	private InputBean input = new InputBean();
	private boolean editable = false;
	private int height = -1;
	private int width = -1;
	private String label;
	private boolean visible = true;
	private String tooltip;
	private String group;
	private int queryPosition = QUERY_POSITION_ABOVE;
	private boolean caseSensitive = false;

	@Override
	public void release() {
		super.release();
		query = null;
		selected = null;
		value = null;
		display = null;
		passthrough = null;
		editable = false;
		height = -1;
		width = -1;
		label = null;
		visible = true;
		tooltip = null;
		group = null;
		queryPosition = QUERY_POSITION_ABOVE;
		caseSensitive = false;
		attributes.clear();
		input = new InputBean();
	}

	/**
	 * @param cssclass The cssclass to set.
	 */
	public void setClass(String cssclass) {
		attributes.setEL(KeyConstants._class, cssclass);
	}

	/**
	 * @param cssstyle The cssstyle to set.
	 */
	public void setStyle(String cssstyle) {
		attributes.setEL(KeyConstants._style, cssstyle);
	}

	/**
	 * @param id The id to set.
	 */
	public void setId(String id) {
		attributes.setEL(KeyConstants._id, id);
	}

	/**
	 * @param multiple The multiple to set.
	 */
	public void setMultiple(String multiple) {
		// alles ausser false ist true
		if (Caster.toBooleanValue(multiple, true)) attributes.setEL("multiple", "multiple");
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		attributes.setEL(KeyConstants._name, name);
		input.setName(name);
	}

	/**
	 * @param size The size to set.
	 */
	public void setSize(double size) {
		attributes.setEL(KeyConstants._size, Caster.toString(size));
	}

	/**
	 * @param tabindex The tabindex to set.
	 */
	public void setTabindex(String tabindex) {
		attributes.setEL("tabindex", tabindex);
	}

	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		attributes.setEL(KeyConstants._title, title);
	}

	/**
	 * @param title The title to set.
	 */
	public void setDir(String dir) {
		attributes.setEL(KeyConstants._dir, dir);
	}

	/**
	 * @param title The title to set.
	 */
	public void setLang(String lang) {
		attributes.setEL(KeyConstants._lang, lang);
	}

	/**
	 * @param onblur The onblur to set.
	 */
	public void setOnblur(String onblur) {
		attributes.setEL("onblur", onblur);
	}

	/**
	 * @param onchange The onchange to set.
	 */
	public void setOnchange(String onchange) {
		attributes.setEL("onchange", onchange);
	}

	/**
	 * @param onclick The onclick to set.
	 */
	public void setOnclick(String onclick) {
		attributes.setEL("onclick", onclick);
	}

	/**
	 * @param ondblclick The ondblclick to set.
	 */
	public void setOndblclick(String ondblclick) {
		attributes.setEL("ondblclick", ondblclick);
	}

	/**
	 * @param onmousedown The onmousedown to set.
	 */
	public void setOnmousedown(String onmousedown) {
		attributes.setEL("onmousedown", onmousedown);
	}

	/**
	 * @param ondblclick The ondblclick to set.
	 */
	public void setOnmouseup(String onmouseup) {
		attributes.setEL("onmouseup", onmouseup);
	}

	/**
	 * @param ondblclick The ondblclick to set.
	 */
	public void setOnmouseover(String onmouseover) {
		attributes.setEL("onmouseover", onmouseover);
	}

	/**
	 * @param ondblclick The ondblclick to set.
	 */
	public void setOnmousemove(String onmousemove) {
		attributes.setEL("onmousemove", onmousemove);
	}

	/**
	 * @param ondblclick The ondblclick to set.
	 */
	public void setOnmouseout(String onmouseout) {
		attributes.setEL("onmouseout", onmouseout);
	}

	/**
	 * @param ondblclick The ondblclick to set.
	 */
	public void setOnkeypress(String onkeypress) {
		attributes.setEL("onkeypress", onkeypress);
	}

	/**
	 * @param ondblclick The ondblclick to set.
	 */
	public void setOnkeydown(String onkeydown) {
		attributes.setEL("onkeydown", onkeydown);
	}

	/**
	 * @param ondblclick The ondblclick to set.
	 */
	public void setOnkeyup(String onkeyup) {
		attributes.setEL("onkeyup", onkeyup);
	}

	/**
	 * @param onfocus The onfocus to set.
	 */
	public void setOnfocus(String onfocus) {
		attributes.setEL("onfocus", onfocus);
	}

	/**
	 * @param message The message to set.
	 */
	public void setMessage(String message) {
		input.setMessage(message);
	}

	/**
	 * @param onerror The onerror to set.
	 */
	public void setOnerror(String onerror) {
		input.setOnError(onerror);
	}

	/**
	 * @param required The required to set.
	 */
	public void setRequired(boolean required) {
		input.setRequired(required);
	}

	/**
	 * @param passthrough The passthrough to set.
	 * @throws PageException
	 */
	public void setPassthrough(Object passthrough) throws PageException {
		if (passthrough instanceof Struct) {
			Struct sct = (Struct) passthrough;
			Iterator<Entry<Key, Object>> it = sct.entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				attributes.setEL(e.getKey(), e.getValue());
			}
		}
		else this.passthrough = Caster.toString(passthrough);
	}

	/**
	 * @param query The query to set.
	 * @throws PageException
	 */
	public void setQuery(String strQuery) throws PageException {
		this.query = Caster.toQuery(pageContext.getVariable(strQuery));
	}

	/**
	 * @param display The display to set.
	 */
	public void setDisplay(String display) {
		this.display = display;
	}

	public void setDataformatas(String dataformatas) throws ApplicationException {
		dataformatas = dataformatas.trim();
		String lcDataformatas = dataformatas.toLowerCase();
		if ("plaintext".equals(lcDataformatas) || "html".equals(lcDataformatas)) {
			attributes.setEL("dataformatas", dataformatas);
		}
		else throw new ApplicationException("attribute dataformatas for tag input has an invalid value [" + dataformatas + "], valid values are [plaintext, html");
	}

	public void setDatafld(String datafld) {
		attributes.setEL("datafld", datafld);
	}

	public void setDatasrc(String datasrc) {
		attributes.setEL("datasrc", datasrc);
	}

	public void setDisabled(String disabled) {
		// alles ausser false ist true
		if (Caster.toBooleanValue(disabled, true)) setDisabled(true);
	}

	private void setDisabled(boolean disabled) {
		if (disabled) attributes.setEL(KeyConstants._disabled, "disabled");
	}

	/**
	 * @param selected The selected to set.
	 */
	public void setSelected(String selected) {
		this.selected = ListUtil.trimItems(ListUtil.listToStringArray(selected, ','));
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int doStartTag() {
		return EVAL_BODY_BUFFERED;
	}

	@Override
	public int doEndTag() throws PageException {
		try {
			_doEndTag();
			return EVAL_PAGE;
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	private void _doEndTag() throws IOException, ExpressionException, PageException {

		// check
		if (query != null) {
			if (value == null) throw new ApplicationException("if you have defined attribute query for tag select, you must also define attribute value");
			else if (!query.containsKey(value)) throw new ApplicationException("invalid value for attribute [value], there is no column in query with name [" + value + "]");

			if (display != null && !query.containsKey(display))
				throw new ApplicationException("invalid value for attribute [display], there is no column in query with name [" + display + "]");

			if (group != null && !query.containsKey(group))
				throw new ApplicationException("invalid value for attribute [group], there is no column in query with name [" + group + "]");
		}

		input.setType(Input.TYPE_SELECT);
		Tag parent = getParent();
		while (parent != null && !(parent instanceof Form)) {
			parent = parent.getParent();
		}
		if (parent instanceof Form) {
			Form form = (Form) parent;
			form.setInput(input);
		}
		else {
			throw new ApplicationException("Tag cfselect must be inside a cfform tag");
		}

		pageContext.forceWrite("<select");

		Iterator<Entry<Key, Object>> it = attributes.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			pageContext.forceWrite(" ");
			pageContext.forceWrite(e.getKey().getString());
			pageContext.forceWrite("=\"");
			pageContext.forceWrite(enc(Caster.toString(e.getValue())));
			pageContext.forceWrite("\"");
		}

		if (passthrough != null) {
			pageContext.forceWrite(" ");
			pageContext.forceWrite(passthrough);
		}
		pageContext.forceWrite(">\n");

		if (bodyContent != null && queryPosition == QUERY_POSITION_BELOW) pageContext.forceWrite(bodyContent.getString());

		// write query options
		if (query != null) {
			int rowCount = query.getRowCount();
			String v, d, currentGroup = null, tmp;
			boolean hasDisplay = display != null;
			boolean hasGroup = group != null;

			for (int i = 1; i <= rowCount; i++) {
				v = Caster.toString(query.getAt(value, i));
				d = hasDisplay ? Caster.toString(query.getAt(display, i)) : v;
				if (hasGroup) {
					tmp = Caster.toString(query.getAt(group, i));
					if (currentGroup == null || !OpUtil.equals(ThreadLocalPageContext.get(), currentGroup, tmp, true)) {
						if (currentGroup != null) pageContext.forceWrite("</optgroup>\n");
						pageContext.forceWrite("<optgroup label=\"" + tmp + "\">\n ");
						currentGroup = tmp;
					}
				}
				pageContext.forceWrite("<option" + selected(v, selected) + " value=\"" + v + "\">" + d + "</option>\n");
			}
			if (hasGroup) pageContext.forceWrite("</optgroup>\n");
		}

		if (bodyContent != null && queryPosition == QUERY_POSITION_ABOVE) pageContext.forceWrite(bodyContent.getString());
		pageContext.forceWrite("</select>");

	}

	private String selected(String str, String[] selected) throws PageException {
		if (selected != null) {
			for (int i = 0; i < selected.length; i++) {
				if (caseSensitive) {
					if (str.compareTo(selected[i]) == 0) return " selected";
				}
				else {
					if (OpUtil.compare(ThreadLocalPageContext.get(), str, selected[i]) == 0) return " selected";
				}
				// if(Operator.compare(str,selected[i])==0) return " selected";
			}
		}
		return "";
	}

	/**
	 * html encode a string
	 * 
	 * @param str string to encode
	 * @return encoded string
	 */
	private String enc(String str) {
		return HTMLEntities.escapeHTML(str, HTMLEntities.HTMLV20);
	}

	/**
	 * @param editable the editable to set
	 * @throws ApplicationException
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * @param group the group to set
	 * @throws ApplicationException
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * @param height the height to set
	 * @throws ApplicationException
	 */
	public void setHeight(double height) {
		this.height = (int) height;
	}

	/**
	 * @param label the label to set
	 * @throws ApplicationException
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @param queryPosition the queryPosition to set
	 * @throws ApplicationException
	 */
	public void setQueryposition(String strQueryPosition) throws ApplicationException {
		strQueryPosition = strQueryPosition.trim().toLowerCase();
		if ("above".equals(strQueryPosition)) queryPosition = QUERY_POSITION_ABOVE;
		else if ("below".equals(strQueryPosition)) queryPosition = QUERY_POSITION_BELOW;
		else throw new ApplicationException("attribute queryPosition for tag select has an invalid value [" + strQueryPosition + "], " + "valid values are [above, below]");

	}

	/**
	 * @param tooltip the tooltip to set
	 * @throws ApplicationException
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	/**
	 * @param visible the visible to set
	 * @throws ApplicationException
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @param width the width to set
	 * @throws ApplicationException
	 */
	public void setWidth(double width) {
		this.width = (int) width;
	}

	/**
	 * @param width the width to set
	 * @throws ApplicationException
	 */
	public void setEnabled(String enabled) {
		setDisabled(!Caster.toBooleanValue(enabled, true));
	}

	/**
	 * @param caseSensitive the caseSensitive to set
	 */
	public void setCasesensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}
}