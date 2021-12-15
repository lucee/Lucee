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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.lang.IDGenerator;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.functions.dynamicEvaluation.DE;
import lucee.runtime.functions.string.JSStringFormat;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

/**
 * implementation of the form tag
 */
public final class Form extends BodyTagImpl {

	public static final int FORMAT_HTML = 0;
	public static final int FORMAT_FLASH = 1;
	public static final int FORMAT_XML = 2;
	private static final String DEFAULT_ARCHIVE = "/lucee/lucee-applet.cfm";

	private static final int WMODE_WINDOW = 0;
	private static final int WMODE_TRANSPARENT = 1;
	private static final int WMODE_OPAQUE = 2;

	// private static int _count=0;
	private int count = 0;
	private String name;
	private String action;
	private boolean preserveData;
	private String onsubmit;
	private String onreset;
	private String onload;
	private String passthrough;
	private String method = "post";
	private String scriptSrc;

	private int format = FORMAT_HTML;

	private Struct attributes = new StructImpl();
	private Map inputs = new LinkedHashMap();
	private String strSkin;
	private String archive = null;
	private String codebase = null;
	private String height = "100%";
	private String width = "100%";
	private boolean preloader = true;
	private int timeout = 0;
	private int wMode = WMODE_WINDOW;
	private boolean accessible = false;
	private String onError;

	@Override
	public void release() {
		super.release();
		name = null;
		action = null;
		preserveData = false;
		attributes.clear();
		onsubmit = null;
		onreset = null;
		onload = null;
		passthrough = null;
		method = "post";
		scriptSrc = null;
		strSkin = null;
		archive = null;
		codebase = null;
		height = "100%";
		width = "100%";
		preloader = true;
		timeout = 0;
		wMode = WMODE_WINDOW;
		accessible = false;
		onError = null;
		inputs.clear();
	}

	/**
	 * @param enablecab The enablecab to set.
	 * @throws ApplicationException
	 */
	public void setEnablecab(boolean enablecab) {
		// DeprecatedUtil.tagAttribute(pageContext,"Form", "enablecab");

	}

	/**
	 * @param method The method to set.
	 * @throws ApplicationException
	 */
	public void setMethod(String method) throws ApplicationException {
		method = method.trim().toLowerCase();
		if (method.equals("get") || method.equals("post")) this.method = method;
		else throw new ApplicationException("invalid value for attribute method from tag form, attribute can have value [get,post] but now is [" + method + "]");
	}

	/**
	 * @param format the format to set
	 * @throws ApplicationException
	 */
	public void setFormat(String strFormat) throws ApplicationException {
		strFormat = strFormat.trim().toLowerCase();

		if ("html".equals(strFormat)) format = FORMAT_HTML;
		else if ("xml".equals(strFormat)) format = FORMAT_XML;
		else if ("flash".equals(strFormat)) format = FORMAT_FLASH;

		else throw new ApplicationException(
				"invalid value [" + strFormat + "] for attribute format, for this attribute only the following values are supported " + "[xml, html, flash]");

		if (format != FORMAT_HTML) throw new ApplicationException("format [" + strFormat + "] is not supported, only the following formats are supported [html]");
		// TODO support other formats
	}

	/**
	 * @param skin The skin to set.
	 */
	public void setSkin(String strSkin) {
		this.strSkin = strSkin;
	}

	/**
	 * @param action The action to set.
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @param scriptSrc The scriptSrc to set.
	 */
	public void setScriptsrc(String scriptSrc) {
		this.scriptSrc = scriptSrc;
	}

	/**
	 * @param archive The archive to set.
	 * @throws ApplicationException
	 */
	public void setArchive(String archive) {
		archive = archive.trim().toLowerCase().replace('\\', '/');
		if (!StringUtil.startsWith(archive, '/')) archive = "/" + archive;
		this.archive = archive;
	}

	/**
	 * @param codebase The codebase to set.
	 * @throws ApplicationException
	 */
	public void setCodebase(String codebase) {
		this.codebase = codebase;
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
	 * @param enctype The enctype to set.
	 */
	public void setEnctype(String enctype) {
		attributes.setEL(KeyConstants._enctype, enctype);
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

	public void setAcceptcharset(String accept_charset) {
		attributes.setEL("accept-charset", accept_charset);
	}

	public void setAccept_charset(String accept_charset) {
		attributes.setEL("accept-charset", accept_charset);
	}

	/**
	 * @param name The name to set.
	 * @throws ApplicationException
	 */
	public void setName(String name) throws ApplicationException {
		this.name = name;
		checkName(name);
	}

	private static void checkName(String name) throws ApplicationException {
		if (name.length() == 0) return;
		int len = name.length();

		for (int pos = 0; pos < len; pos++) {
			char c = name.charAt(pos);
			if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '_') || (c == '-') || (c == ':') || (c == '.')) continue;
			throw new ApplicationException("value of attribute name [" + name + "] is invalid, only the following characters are allowed [a-z,A-Z,0-9,-,_,:,.]");
		}
	}

	/**
	 * @param onreset The onreset to set.
	 */
	public void setOnreset(String onreset) {
		this.onreset = onreset;
	}

	/**
	 * @param onreset The onreset to set.
	 */
	public void setOnload(String onload) {
		this.onload = onload;
	}

	/**
	 * @param onsubmit The onsubmit to set.
	 */
	public void setOnsubmit(String onsubmit) {
		this.onsubmit = onsubmit;
	}

	public void setOnerror(String onError) {
		this.onError = onError;
	}

	public void setOnclick(String onclick) {
		attributes.setEL("onclick", onclick);
	}

	public void setOndblclick(String ondblclick) {
		attributes.setEL("ondblclick", ondblclick);
	}

	public void setOnmousedown(String onmousedown) {
		attributes.setEL("onmousedown", onmousedown);
	}

	public void setOnmouseup(String onmouseup) {
		attributes.setEL("onmouseup", onmouseup);
	}

	public void setOnmouseover(String onmouseover) {
		attributes.setEL("onmouseover", onmouseover);
	}

	public void setOnmousemove(String onmousemove) {
		attributes.setEL("onmousemove", onmousemove);
	}

	public void setOnmouseout(String onmouseout) {
		attributes.setEL("onmouseout", onmouseout);
	}

	public void setOnkeypress(String onkeypress) {
		attributes.setEL("onkeypress", onkeypress);
	}

	public void setOnkeydown(String onkeydown) {
		attributes.setEL("onkeydown", onkeydown);
	}

	public void setOnkeyup(String onkeyup) {
		attributes.setEL("onkeyup", onkeyup);
	}

	/**
	 * @param passthrough The passthrough to set.
	 * @throws PageException
	 */
	public void setPassthrough(Object passthrough) throws PageException {
		if (passthrough instanceof Struct) {
			Struct sct = (Struct) passthrough;
			// lucee.runtime.type.Collection.Key[] keys=sct.keys();
			// lucee.runtime.type.Collection.Key key;
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
	 * @param preserveData The preserveData to set.
	 * @throws ApplicationException
	 */
	public void setPreservedata(boolean preserveData) throws ApplicationException {
		// this.preserveData = preserveData;
		if (preserveData) throw new ApplicationException("attribute preserveData for tag form is not supported at the moment");
	}

	/**
	 * @param target The target to set.
	 */
	public void setTarget(String target) {
		attributes.setEL(KeyConstants._target, target);
	}

	public void setTitle(String title) {
		attributes.setEL(KeyConstants._title, title);
	}

	public void setDir(String dir) {
		attributes.setEL(KeyConstants._dir, dir);
	}

	public void setLang(String lang) {
		attributes.setEL(KeyConstants._lang, lang);
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(String height) {
		this.height = height;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(String width) {
		this.width = width;
	}

	/**
	 * @param preloader the preloader to set
	 */
	public void setPreloader(boolean preloader) {
		this.preloader = preloader;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(double timeout) {
		this.timeout = (int) timeout;
	}

	/**
	 * @param strWMode the wmode to set
	 * @throws ApplicationException
	 */
	public void setWmode(String strWMode) throws ApplicationException {
		strWMode = strWMode.toLowerCase().trim();
		if ("window".equals(strWMode)) wMode = WMODE_WINDOW;
		else if ("transparent".equals(strWMode)) wMode = WMODE_TRANSPARENT;
		else if ("opaque".equals(strWMode)) wMode = WMODE_OPAQUE;

		else throw new ApplicationException(
				"invalid value [" + strWMode + "] for attribute wmode, for this attribute only the following values are supported " + "[window, transparent, opaque]");
	}

	/**
	 * @param strWMode the wmode to set
	 */
	public void setAccessible(boolean accessible) {
		this.accessible = accessible;
	}

	@Override
	public int doStartTag() throws PageException {

		try {
			return _doStartTag();
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	private int _doStartTag() throws PageException, IOException {
		String contextPath = pageContext.getHttpServletRequest().getContextPath();
		if (contextPath == null) contextPath = "";
		if (archive == null) {
			archive = contextPath + DEFAULT_ARCHIVE;
		}
		count = IDGenerator.intId();

		if (name == null) {
			name = "CFForm_" + count;
		}
		attributes.setEL(KeyConstants._name, name);

		if (action == null) action = ReqRspUtil.self(pageContext.getHttpServletRequest());
		attributes.setEL(KeyConstants._action, action);

		String suffix = StringUtil.isEmpty(name) ? "" + count : StringUtil.toVariableName(name);
		String funcName = "lucee_form_" + count;

		String checkName = "_CF_check" + suffix;
		String resetName = "_CF_reset" + suffix;
		String loadName = "_CF_load" + suffix;

		// boolean hasListener=false;
		if (onsubmit == null) attributes.setEL("onsubmit", "return " + funcName + ".check();");
		else {
			attributes.setEL("onsubmit", "return " + checkName + "();");
			// hasListener=true;
		}
		if (onreset != null) {
			attributes.setEL("onreset", resetName + "();");
			// hasListener=true;
		}
		if (onload != null) {
			attributes.setEL("onload", loadName + "();");
			// hasListener=true;
		}

		if (scriptSrc == null) scriptSrc = contextPath + "/lucee/form.cfm";
		attributes.setEL(KeyConstants._method, method);

		pageContext.forceWrite("<script language = \"JavaScript\" type=\"text/javascript\" src=\"" + scriptSrc + "\"></script>");
		// if(hasListener) {
		pageContext.forceWrite("<script language = \"JavaScript\" type=\"text/javascript\">\n");
		if (onsubmit != null) pageContext.forceWrite("function " + checkName + "() { if(" + funcName + ".check()){" + onsubmit + "\nreturn true;}else {return false;}}\n");
		else pageContext.forceWrite("function " + checkName + "() { return " + funcName + ".check();}\n");

		if (onreset != null) pageContext.forceWrite("function " + resetName + "() {" + onreset + "\n}\n");
		if (onload != null) pageContext.forceWrite("function " + loadName + "() {" + onload + "\n}\n");
		pageContext.forceWrite("\n</script>");

		// }
		pageContext.forceWrite("<form");

		Iterator<Entry<Key, Object>> it = attributes.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			pageContext.forceWrite(" ");
			pageContext.forceWrite(e.getKey().getString());
			pageContext.forceWrite("=");
			pageContext.forceWrite(de(Caster.toString(e.getValue())));

		}

		if (passthrough != null) {
			pageContext.forceWrite(" ");
			pageContext.forceWrite(passthrough);
		}
		pageContext.forceWrite(">");

		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() throws PageException {
		String funcName = "lucee_form_" + count;
		try {
			pageContext.forceWrite("</form><!-- name:" + name + " --><script>\n");
			pageContext.forceWrite(funcName + "=new LuceeForms(" + js(name) + "," + js(onError) + ");\n");
			Iterator it = inputs.keySet().iterator();
			while (it.hasNext()) {
				InputBean input = (InputBean) inputs.get(it.next());

				pageContext.forceWrite(funcName + ".addInput(" + js(input.getName()) + "," + input.isRequired() + "," + input.getType() + "," + input.getValidate() + ","
						+ (input.getPattern()) + "," + js(input.getMessage()) + "," + js(input.getOnError()) + "," + js(input.getOnValidate()) + "," + range(input.getRangeMin())
						+ "," + range(input.getRangeMax()) + "," + (input.getMaxLength()) + ");\n");
			}
			pageContext.forceWrite("</script>");
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return EVAL_PAGE;
	}

	private String range(double range) {
		if (!Decision.isValid(range)) return "null";
		return Caster.toString(range);
	}

	private String de(String str) throws PageException {
		return DE.call(pageContext, str);
	}

	private String js(String str) {
		if (str == null) return "null";
		return "'" + JSStringFormat.call(pageContext, str) + "'";
	}

	/**
	 * @param input
	 * @throws ApplicationException
	 */
	public void setInput(InputBean input) throws ApplicationException {
		if (input.getType() == Input.TYPE_TEXT || input.getType() == Input.TYPE_PASSWORD) {
			InputBean i = (InputBean) inputs.get(input.getName().toLowerCase());
			if (i != null && (i.getType() == Input.TYPE_TEXT || i.getType() == Input.TYPE_PASSWORD)) {
				throw new ApplicationException("duplicate input field [" + i.getName() + "] for form", "a text or password field must be unique");
			}
		}

		// if(StringUtil.isEmpty(input.getOnError(),true) && !StringUtil.isEmpty(onError,true))
		// input.setOnError(onError);

		inputs.put(input.getName().toLowerCase(), input);
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the onsubmit.
	 */
	public String getOnsubmit() {
		return onsubmit;
	}

	public int getFormat() {
		return format;
	}

	public String getArchive() {
		return archive;
	}

	public String getCodebase() {
		return codebase;
	}
}