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
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

// FUTURE tag input 
//attr validateAt impl tag atrr
//attr validate add support for submitOnce
// Added support for generating Flash and XML controls (specified in the cfform tag).
// Added support for preventing multiple submissions.
// attr mask impl. logik dahinter umsetzen

/**
 * 
 */
public class Input extends TagImpl {

	public static final short TYPE_SELECT = -1;
	public static final short TYPE_TEXT = 0;
	public static final short TYPE_RADIO = 1;
	public static final short TYPE_CHECKBOX = 2;
	public static final short TYPE_PASSWORD = 3;
	public static final short TYPE_BUTTON = 4;
	public static final short TYPE_FILE = 5;
	public static final short TYPE_HIDDEN = 6;
	public static final short TYPE_IMAGE = 7;
	public static final short TYPE_RESET = 8;
	public static final short TYPE_SUBMIT = 9;
	public static final short TYPE_DATEFIELD = 10;

	public static final short VALIDATE_DATE = 4;
	public static final short VALIDATE_EURODATE = 5;
	public static final short VALIDATE_TIME = 6;
	public static final short VALIDATE_FLOAT = 7;
	public static final short VALIDATE_INTEGER = 8;
	public static final short VALIDATE_TELEPHONE = 9;
	public static final short VALIDATE_ZIPCODE = 10;
	public static final short VALIDATE_CREDITCARD = 11;
	public static final short VALIDATE_SOCIAL_SECURITY_NUMBER = 12;
	public static final short VALIDATE_REGULAR_EXPRESSION = 13;
	public static final short VALIDATE_NONE = 14;

	public static final short VALIDATE_USDATE = 15;
	public static final short VALIDATE_RANGE = 16;
	public static final short VALIDATE_BOOLEAN = 17;
	public static final short VALIDATE_EMAIL = 18;
	public static final short VALIDATE_URL = 19;
	public static final short VALIDATE_UUID = 20;
	public static final short VALIDATE_GUID = 21;
	public static final short VALIDATE_MAXLENGTH = 22;
	public static final short VALIDATE_NOBLANKS = 23;
	// TODO SubmitOnce

	/**
	 * @param validate The validate to set.
	 * @throws ApplicationException
	 */
	public void setValidate(String validate) throws ApplicationException {
		validate = validate.toLowerCase().trim();
		if (validate.equals("creditcard")) input.setValidate(VALIDATE_CREDITCARD);
		else if (validate.equals("date")) input.setValidate(VALIDATE_DATE);
		else if (validate.equals("usdate")) input.setValidate(VALIDATE_USDATE);
		else if (validate.equals("eurodate")) input.setValidate(VALIDATE_EURODATE);
		else if (validate.equals("float")) input.setValidate(VALIDATE_FLOAT);
		else if (validate.equals("numeric")) input.setValidate(VALIDATE_FLOAT);
		else if (validate.equals("integer")) input.setValidate(VALIDATE_INTEGER);
		else if (validate.equals("int")) input.setValidate(VALIDATE_INTEGER);
		else if (validate.equals("regular_expression")) input.setValidate(VALIDATE_REGULAR_EXPRESSION);
		else if (validate.equals("regex")) input.setValidate(VALIDATE_REGULAR_EXPRESSION);
		else if (validate.equals("social_security_number")) input.setValidate(VALIDATE_SOCIAL_SECURITY_NUMBER);
		else if (validate.equals("ssn")) input.setValidate(VALIDATE_SOCIAL_SECURITY_NUMBER);
		else if (validate.equals("telephone")) input.setValidate(VALIDATE_TELEPHONE);
		else if (validate.equals("phone")) input.setValidate(VALIDATE_TELEPHONE);
		else if (validate.equals("time")) input.setValidate(VALIDATE_TIME);
		else if (validate.equals("zipcode")) input.setValidate(VALIDATE_ZIPCODE);
		else if (validate.equals("zip")) input.setValidate(VALIDATE_ZIPCODE);

		else if (validate.equals("range")) input.setValidate(VALIDATE_RANGE);
		else if (validate.equals("boolean")) input.setValidate(VALIDATE_BOOLEAN);
		else if (validate.equals("email")) input.setValidate(VALIDATE_EMAIL);
		else if (validate.equals("url")) input.setValidate(VALIDATE_URL);
		else if (validate.equals("uuid")) input.setValidate(VALIDATE_UUID);
		else if (validate.equals("guid")) input.setValidate(VALIDATE_GUID);
		else if (validate.equals("maxlength")) input.setValidate(VALIDATE_MAXLENGTH);
		else if (validate.equals("noblanks")) input.setValidate(VALIDATE_NOBLANKS);

		else throw new ApplicationException("attribute validate has an invalid value [" + validate + "]",
				"valid values for attribute validate are [creditcard, date, eurodate, float, integer, regular, social_security_number, telephone, time, zipcode]");

	}

	public static final String[] DAYNAMES_DEFAULT = new String[] { "S", "M", "T", "W", "Th", "F", "S" };
	public static final String[] MONTHNAMES_DEFAULT = new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November",
			"December" };

	Struct attributes = new StructImpl();
	InputBean input = new InputBean();
	String passthrough;

	String[] daynames = DAYNAMES_DEFAULT;
	String[] monthnames = MONTHNAMES_DEFAULT;

	boolean enabled = true;
	boolean visible = true;
	String label;
	String tooltip;
	String validateAt;
	double firstDayOfWeek = 0;
	String mask;
	boolean encodeValue = true;

	@Override
	public void release() {
		super.release();
		input = new InputBean();
		attributes.clear();
		passthrough = null;

		daynames = DAYNAMES_DEFAULT;
		monthnames = MONTHNAMES_DEFAULT;
		enabled = true;
		visible = true;
		label = null;
		tooltip = null;
		validateAt = null;
		firstDayOfWeek = 0;
		mask = null;
		encodeValue = true;
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

	public void setAccept(String accept) {
		attributes.setEL(KeyConstants._accept, accept);
	}

	public void setAccesskey(String accesskey) {
		attributes.setEL("accesskey", accesskey);
	}

	public void setAlign(String align) {
		attributes.setEL(KeyConstants._align, align);
	}

	public void setAlt(String alt) {
		attributes.setEL(KeyConstants._alt, alt);
	}

	public void setAutocomplete(String autocomplete) {
		attributes.setEL("autocomplete", autocomplete);
	}

	public void setAutofocus(String autofocus) {
		attributes.setEL("autofocus", autofocus);
	}

	public void setBorder(String border) {
		attributes.setEL(KeyConstants._border, border);
	}

	public void setDatafld(String datafld) {
		attributes.setEL("datafld", datafld);
	}

	public void setDatasrc(String datasrc) {
		attributes.setEL("datasrc", datasrc);
	}

	public void setForm(String form) {
		attributes.setEL(KeyConstants._form, form);
	}

	public void setFormaction(String formAction) {
		attributes.setEL("formaction", formAction);
	}

	public void setFormenctype(String formenctype) {
		attributes.setEL("formenctype", formenctype);
	}

	public void setFormmethod(String formmethod) {
		attributes.setEL("formmethod", formmethod);
	}

	public void setFormnovalidate(String formnovalidate) {
		attributes.setEL("formnovalidate", formnovalidate);
	}

	public void setFormtarget(String formtarget) {
		attributes.setEL("formtarget", formtarget);
	}

	public void setLang(String lang) {
		attributes.setEL(KeyConstants._lang, lang);
	}

	public void setList(String list) {
		attributes.setEL(KeyConstants._list, list);
	}

	public void setDir(String dir) {
		// dir=dir.trim();
		// String lcDir=dir.toLowerCase();
		// if( "ltr".equals(lcDir) || "rtl".equals(lcDir))
		attributes.setEL(KeyConstants._dir, dir);

		// else throw new ApplicationException("attribute dir for tag input has an invalid value ["+dir+"],
		// valid values are [ltr, rtl]");
	}

	public void setDataformatas(String dataformatas) {
		dataformatas = dataformatas.trim();
		// String lcDataformatas=dataformatas.toLowerCase();
		// if( "plaintext".equals(lcDataformatas) || "html".equals(lcDataformatas))
		attributes.setEL("dataformatas", dataformatas);

		// else throw new ApplicationException("attribute dataformatas for tag input has an invalid value
		// ["+dataformatas+"], valid values are [plaintext, html");
	}

	public void setDisabled(String disabled) {
		// alles ausser false ist true
		// if(Caster.toBooleanValue(disabled,true))
		attributes.setEL("disabled", disabled);
	}

	public void setEnabled(String enabled) {
		// alles ausser false ist true
		// setDisabled(Caster.toString(!Caster.toBooleanValue(enabled,true)));
		attributes.setEL("enabled", enabled);
	}

	public void setIsmap(String ismap) {
		// alles ausser false ist true
		// if(Caster.toBooleanValue(ismap,true)) attributes.setEL("ismap","ismap");
		attributes.setEL("ismap", ismap);
	}

	public void setReadonly(String readonly) {
		// alles ausser false ist true
		// if(Caster.toBooleanValue(readonly,true)) attributes.setEL("readonly","readonly");
		attributes.setEL(KeyConstants._readonly, readonly);
	}

	public void setUsemap(String usemap) {
		attributes.setEL("usemap", usemap);
	}

	/**
	 * @param onBlur The onBlur to set.
	 */
	public void setOnblur(String onBlur) {
		attributes.setEL("onblur", onBlur);
	}

	/**
	 * @param onChange The onChange to set.
	 */
	public void setOnchange(String onChange) {
		attributes.setEL("onchange", onChange);
	}

	/**
	 * @param onClick The onClick to set.
	 */
	public void setOnclick(String onClick) {
		attributes.setEL("onclick", onClick);
	}

	/**
	 * @param onDblclick The onDblclick to set.
	 */
	public void setOndblclick(String onDblclick) {
		attributes.setEL("ondblclick", onDblclick);
	}

	/**
	 * @param onFocus The onFocus to set.
	 */
	public void setOnfocus(String onFocus) {
		attributes.setEL("onfocus", onFocus);
	}

	/**
	 * @param onKeyDown The onKeyDown to set.
	 */
	public void setOnkeydown(String onKeyDown) {
		attributes.setEL("onkeydown", onKeyDown);
	}

	/**
	 * @param onKeyPress The onKeyPress to set.
	 */
	public void setOnkeypress(String onKeyPress) {
		attributes.setEL("onkeypress", onKeyPress);
	}

	/**
	 * @param onKeyUp The onKeyUp to set.
	 */
	public void setOnkeyup(String onKeyUp) {
		attributes.setEL("onKeyUp", onKeyUp);
	}

	/**
	 * @param onMouseDown The onMouseDown to set.
	 */
	public void setOnmousedown(String onMouseDown) {
		attributes.setEL("onMouseDown", onMouseDown);
	}

	/**
	 * @param onMouseMove The onMouseMove to set.
	 */
	public void setOnmousemove(String onMouseMove) {
		attributes.setEL("onMouseMove", onMouseMove);
	}

	/**
	 * @param onMouseUp The onMouseUp to set.
	 */
	public void setOnmouseup(String onMouseUp) {
		attributes.setEL("onMouseUp", onMouseUp);
	}

	/**
	 * @param onMouseUp The onMouseUp to set.
	 */
	public void setOnselect(String onselect) {
		attributes.setEL("onselect", onselect);
	}

	/**
	 * @param onMouseOut The onMouseOut to set.
	 */
	public void setOnmouseout(String onMouseOut) {
		attributes.setEL("onMouseOut", onMouseOut);
	}

	/**
	 * @param onMouseOver The onKeyPress to set.
	 */
	public void setOnmouseover(String onMouseOver) {
		attributes.setEL("onMouseOver", onMouseOver);
	}

	/**
	 * @param tabIndex The tabIndex to set.
	 */
	public void setTabindex(String tabIndex) {
		attributes.setEL("tabindex", tabIndex);
	}

	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		attributes.setEL(KeyConstants._title, title);
	}

	/**
	 * @param value The value to set.
	 */
	public void setValue(String value) {
		attributes.setEL(KeyConstants._value, value);
	}

	/**
	 * @param size The size to set.
	 */
	public void setSize(String size) {
		attributes.setEL(KeyConstants._size, size);
	}

	/**
	 * @param maxLength The maxLength to set.
	 */
	public void setMaxlength(double maxLength) {
		input.setMaxLength((int) maxLength);
		attributes.setEL("maxLength", Caster.toString(maxLength));
	}

	/**
	 * @param checked The checked to set.
	 */
	public void setChecked(String checked) {
		// alles ausser false ist true
		if (Caster.toBooleanValue(checked, true)) attributes.setEL("checked", "checked");
	}

	/**
	 * @param daynames The daynames to set.
	 * @throws ApplicationException
	 */
	public void setDaynames(String listDaynames) throws ApplicationException {
		String[] arr = ListUtil.listToStringArray(listDaynames, ',');
		if (arr.length != 7) throw new ApplicationException("value of attribute [daynames] must contain a string list with 7 values, now there are " + arr.length + " values");
		this.daynames = arr;
	}

	/**
	 * @param daynames The daynames to set.
	 * @throws ApplicationException
	 */
	public void setFirstdayofweek(double firstDayOfWeek) throws ApplicationException {
		if (firstDayOfWeek < 0 || firstDayOfWeek > 6) throw new ApplicationException("value of attribute [firstDayOfWeek] must contain a numeric value between 0-6");
		this.firstDayOfWeek = firstDayOfWeek;
	}

	/**
	 * @param daynames The daynames to set.
	 * @throws ApplicationException
	 */
	public void setMonthnames(String listMonthNames) throws ApplicationException {
		String[] arr = ListUtil.listToStringArray(listMonthNames, ',');
		if (arr.length == 12) throw new ApplicationException("value of attribute [MonthNames] must contain a string list with 12 values, now there are " + arr.length + " values");
		this.monthnames = arr;
	}

	/**
	 * @param daynames The daynames to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @param daynames The daynames to set.
	 */
	public void setMask(String mask) {
		this.mask = mask;
	}

	public void setMax(String max) {
		attributes.setEL(KeyConstants._max, max);
	}

	public void setMin(String min) {
		attributes.setEL(KeyConstants._min, min);
	}

	public void setMultiple(String multiple) {
		attributes.setEL(KeyConstants._multiple, multiple);
	}

	public void setPlaceholder(String placeholder) {
		attributes.setEL("placeholder", placeholder);
	}

	/**
	 * @param daynames The daynames to set.
	 */
	public void setNotab(String notab) {
		attributes.setEL("notab", notab);
	}

	/**
	 * @param daynames The daynames to set.
	 */
	public void setHspace(String hspace) {
		attributes.setEL("hspace", hspace);
	}

	/**
	 * @param type The type to set.
	 * @throws ApplicationException
	 */
	public void setType(String type) throws ApplicationException {
		type = type.toLowerCase().trim();
		if ("checkbox".equals(type)) input.setType(TYPE_CHECKBOX);
		else if ("password".equals(type)) input.setType(TYPE_PASSWORD);
		else if ("text".equals(type)) input.setType(TYPE_TEXT);
		else if ("radio".equals(type)) input.setType(TYPE_RADIO);
		else if ("button".equals(type)) input.setType(TYPE_BUTTON);
		else if ("file".equals(type)) input.setType(TYPE_FILE);
		else if ("hidden".equals(type)) input.setType(TYPE_HIDDEN);
		else if ("image".equals(type)) input.setType(TYPE_IMAGE);
		else if ("reset".equals(type)) input.setType(TYPE_RESET);
		else if ("submit".equals(type)) input.setType(TYPE_SUBMIT);
		else if ("datefield".equals(type)) input.setType(TYPE_DATEFIELD);

		else throw new ApplicationException("attribute type has an invalid value [" + type + "]",
				"valid values for attribute type are " + "[checkbox, password, text, radio, button, file, hidden, image, reset, submit, datefield]");

		attributes.setEL(KeyConstants._type, type);
	}

	/**
	 * @param onError The onError to set.
	 */
	public void setOnerror(String onError) {
		input.setOnError(onError);
	}

	/**
	 * @param onValidate The onValidate to set.
	 */
	public void setOnvalidate(String onValidate) {
		input.setOnValidate(onValidate);
	}

	/**
	 * @param passthrough The passThrough to set.
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

		// input.setPassThrough(passThrough);
	}

	/**
	 * @param pattern The pattern to set.
	 * @throws ExpressionException
	 */
	public void setPattern(String pattern) throws ExpressionException {
		input.setPattern(pattern);
	}

	/**
	 * @param range The range to set.
	 * @throws PageException
	 */
	public void setRange(String range) throws PageException {
		String errMessage = "attribute range has an invalid value [" + range + "], must be string list with numbers";
		String errDetail = "Example: [number_from,number_to], [number_from], [number_from,], [,number_to]";

		Array arr = ListUtil.listToArray(range, ',');

		if (arr.size() == 1) {
			double from = Caster.toDoubleValue(arr.get(1, null), true, Double.NaN);
			if (!Decision.isValid(from)) throw new ApplicationException(errMessage, errDetail);
			input.setRangeMin(from);
			input.setRangeMax(Double.NaN);
		}
		else if (arr.size() == 2) {
			String strFrom = arr.get(1, "").toString().trim();
			double from = Caster.toDoubleValue(strFrom, Double.NaN);
			if (!Decision.isValid(from) && strFrom.length() > 0) {
				throw new ApplicationException(errMessage, errDetail);
			}
			input.setRangeMin(from);

			String strTo = arr.get(2, "").toString().trim();
			double to = Caster.toDoubleValue(strTo, Double.NaN);
			if (!Decision.isValid(to) && strTo.length() > 0) {
				throw new ApplicationException(errMessage, errDetail);
			}
			input.setRangeMax(to);

		}
		else throw new ApplicationException(errMessage, errDetail);
	}

	/**
	 * @param required The required to set.
	 */
	public void setRequired(boolean required) {
		input.setRequired(required);
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		attributes.setEL(KeyConstants._name, name);
		input.setName(name);
	}

	/**
	 * @param message The message to set.
	 */
	public void setMessage(String message) {
		if (!StringUtil.isEmpty(message)) input.setMessage(message);
	}

	/**
	 * @param encodeValue Encode value using HTMLEntities.escapeHTML, or allow using htmlEncodeForAttribute()
	 */
	public void setEncodevalue(boolean encodeValue) {
		this.encodeValue = encodeValue; 
	}

	@Override
	public int doEndTag() throws PageException {
		try {
			_doEndTag();
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return EVAL_PAGE;
	}

	private void _doEndTag() throws PageException, IOException {
		// check attributes
		if (input.getValidate() == VALIDATE_REGULAR_EXPRESSION && input.getPattern() == null) {
			throw new ApplicationException("when validation type regular_expression is selected, the pattern attribute is required");
		}

		Tag parent = getParent();
		while (parent != null && !(parent instanceof Form)) {
			parent = parent.getParent();
		}
		if (parent instanceof Form) {
			Form form = (Form) parent;
			form.setInput(input);
			if (input.getType() == TYPE_DATEFIELD && form.getFormat() != Form.FORMAT_FLASH)
				throw new ApplicationException("type [datefield] is only allowed if form format is flash");
		}
		else {
			throw new ApplicationException("Tag must be inside a form tag");
		}
		draw();
	}

	void draw() throws IOException, PageException {

		// start output
		pageContext.forceWrite("<input");

		// lucee.runtime.type.Collection.Key[] keys = attributes.keys();
		// lucee.runtime.type.Collection.Key key;
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
		pageContext.forceWrite(">");
	}

	/**
	 * html encode a string
	 * 
	 * @param str string to encode
	 * @return encoded string
	 */
	String enc(String str) {
		if (encodeValue) return HTMLEntities.escapeHTML(str, HTMLEntities.HTMLV20);
		else return str;
	}

	/**
	 * @return the monthnames
	 */
	public String[] getMonthnames() {
		return monthnames;
	}

	/**
	 * @param monthnames the monthnames to set
	 */
	public void setMonthnames(String[] monthnames) {
		this.monthnames = monthnames;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(String height) {
		attributes.setEL(KeyConstants._height, height);
	}

	/**
	 * @param input the input to set
	 */
	public void setInput(InputBean input) {
		this.input = input;
	}

	/**
	 * @param passthrough the passthrough to set
	 */
	public void setPassthrough(String passthrough) {
		this.passthrough = passthrough;
	}

	/**
	 * @param tooltip the tooltip to set
	 * @throws ApplicationException
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	/**
	 * @param validateAt the validateAt to set
	 * @throws ApplicationException
	 */
	public void setValidateat(String validateAt) throws ApplicationException {
		this.validateAt = validateAt;
		throw new ApplicationException("attribute [validateAt] is not supported for tag input ");

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
	public void setWidth(String width) {
		attributes.setEL(KeyConstants._width, width);
	}

	private ExpressionException notSupported(String label) {
		return new ExpressionException("attribute [" + label + "] is not supported");
	}

	public void setAutosuggest(String autosuggest) throws ExpressionException {
		throw notSupported("autosuggest");
		// attributes.setEL("bind",bind);
	}

	public void setAutosuggestbinddelay(double autosuggestBindDelay) throws ExpressionException {
		throw notSupported("autosuggestBindDelay");
		// attributes.setEL("bind",bind);
	}

	public void setAutosuggestminlength(double autosuggestMinLength) throws ExpressionException {
		throw notSupported("autosuggestMinLength");
		// attributes.setEL("bind",bind);
	}

	public void setBind(String bind) throws ExpressionException {
		throw notSupported("bind");
		// attributes.setEL("bind",bind);
	}

	public void setBindattribute(String bindAttribute) throws ExpressionException {
		throw notSupported("bindAttribute");
		// attributes.setEL("bind",bind);
	}

	public void setBindonload(boolean bindOnLoad) throws ExpressionException {
		throw notSupported("bindOnLoad");
		// attributes.setEL("bind",bind);
	}

	public void setDelimiter(String delimiter) throws ExpressionException {
		throw notSupported("delimiter");
		// attributes.setEL("bind",bind);
	}

	public void setMaxresultsdisplayed(double maxResultsDisplayed) throws ExpressionException {
		throw notSupported("maxResultsDisplayed");
		// attributes.setEL("bind",bind);
	}

	public void setOnbinderror(String onBindError) throws ExpressionException {
		throw notSupported("onBindError");
		// attributes.setEL("bind",bind);
	}

	public void setShowautosuggestloadingicon(boolean showAutosuggestLoadingIcon) throws ExpressionException {
		throw notSupported("showAutosuggestLoadingIcon");
		// attributes.setEL("bind",bind);
	}

	public void setSourcefortooltip(String sourceForTooltip) throws ExpressionException {
		throw notSupported("sourceForTooltip");
		// attributes.setEL("bind",bind);
	}

	public void setSrc(String src) {
		attributes.setEL(KeyConstants._src, src);
	}

	public void setStep(String step) {
		attributes.setEL(KeyConstants._step, step);
	}

	public void setTypeahead(boolean typeahead) throws ExpressionException {
		throw notSupported("typeahead");
		// attributes.setEL("src",src);
	}

}