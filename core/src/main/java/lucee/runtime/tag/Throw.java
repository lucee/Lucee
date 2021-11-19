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

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.CatchBlock;
import lucee.runtime.exp.CustomTypeException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageExceptionImpl;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

/**
 * The cfthrow tag raises a developer-specified exception that can be caught with cfcatch tag having
 * any of the following type specifications - cfcatch type = 'custom_type', cfcatch type =
 * 'Application' 'cfcatch' type = 'Any'
 *
 *
 *
 **/
public final class Throw extends TagImpl {

	/** A custom error code that you supply. */
	private String extendedinfo = null;

	private String type = "application";

	private String detail = "";

	/** A message that describes the exceptional event. */
	private Object message;

	/** A custom error code that you supply. */
	private String errorcode = "";

	private Object object;

	private int level = 1;

	@Override
	public void release() {
		super.release();
		extendedinfo = null;
		type = "application";
		detail = "";
		message = null;
		errorcode = "";
		object = null;
		level = 1;
	}

	/**
	 * set the value extendedinfo A custom error code that you supply.
	 * 
	 * @param extendedinfo value to set
	 **/
	public void setExtendedinfo(String extendedinfo) {
		this.extendedinfo = extendedinfo;
	}

	/**
	 * set the value type
	 * 
	 * @param type value to set
	 **/
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * set the value detail
	 * 
	 * @param detail value to set
	 **/
	public void setDetail(String detail) {
		this.detail = detail;
	}

	/**
	 * set the value message A message that describes the exceptional event.
	 * 
	 * @param message value to set
	 **/
	public void setMessage(Object message) {
		this.message = message;
	}

	/**
	 * @deprecated this method should no longer be used.
	 */
	@Deprecated
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * set the value errorcode A custom error code that you supply.
	 * 
	 * @param errorcode value to set
	 **/
	public void setErrorcode(String errorcode) {
		this.errorcode = errorcode;
	}

	/**
	 * set the value object a native java exception Object, if this attribute is defined all other will
	 * be ignored.
	 * 
	 * @param object object to set
	 * @throws PageException
	 **/
	public void setObject(Object object) throws PageException {
		this.object = object;
	}

	public void setContextlevel(double level) {
		this.level = (int) level;
	}

	public static PageException toPageException(Object object, PageException defaultValue) throws PageException {
		if ((object instanceof ObjectWrap)) return toPageException(((ObjectWrap) object).getEmbededObject(), defaultValue);

		if (object instanceof CatchBlock) {
			CatchBlock cb = (CatchBlock) object;
			return cb.getPageException();
		}
		if (object instanceof PageException) return (PageException) object;
		if (object instanceof Throwable) {
			Throwable t = (Throwable) object;
			return new CustomTypeException(t.getMessage(), "", "", t.getClass().getName(), "");
		}
		if (object instanceof Struct) {
			Struct sct = (Struct) object;
			String type = Caster.toString(sct.get(KeyConstants._type, ""), "").trim();
			String msg = Caster.toString(sct.get(KeyConstants._message, null), null);
			if (!StringUtil.isEmpty(msg, true)) {
				String detail = Caster.toString(sct.get(KeyConstants._detail, null), null);
				String errCode = Caster.toString(sct.get("ErrorCode", null), null);
				String extInfo = Caster.toString(sct.get("ExtendedInfo", null), null);

				PageException pe = null;
				if ("application".equalsIgnoreCase(type)) pe = new ApplicationException(msg, detail);
				else if ("expression".equalsIgnoreCase(type)) pe = new ExpressionException(msg, detail);
				else pe = new CustomTypeException(msg, detail, errCode, type, extInfo);

				// Extended Info
				if (!StringUtil.isEmpty(extInfo, true)) pe.setExtendedInfo(extInfo);

				// Error Code
				if (!StringUtil.isEmpty(errCode, true)) pe.setErrorCode(errCode);

				// Additional
				if (pe instanceof PageExceptionImpl) {
					PageExceptionImpl pei = (PageExceptionImpl) pe;
					sct = Caster.toStruct(sct.get("additional", null), null);
					if (sct != null) {
						Iterator<Entry<Key, Object>> it = sct.entryIterator();
						Entry<Key, Object> e;
						while (it.hasNext()) {
							e = it.next();
							pei.setAdditional(e.getKey(), e.getValue());
						}
					}
				}
				return pe;
			}
		}

		return defaultValue;

	}

	@Override
	public int doStartTag() throws PageException {

		_doStartTag(message);
		_doStartTag(object);

		throw new CustomTypeException("", detail, errorcode, type, extendedinfo, level);
	}

	private void _doStartTag(Object obj) throws PageException {
		if (!StringUtil.isEmpty(obj)) {
			PageException pe = toPageException(obj, null);
			if (pe != null) throw pe;

			CustomTypeException exception = new CustomTypeException(Caster.toString(obj), detail, errorcode, type, extendedinfo, level);
			throw exception;
		}
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}
}