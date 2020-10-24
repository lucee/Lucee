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
package lucee.runtime.type.scope;

import java.io.UnsupportedEncodingException;

import lucee.commons.net.URLItem;
import lucee.runtime.PageContext;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;

/**
 * Implements URL Scope
 */
public final class URLImpl extends ScopeSupport implements URL, ScriptProtected {

	private String encoding = null;
	private int scriptProtected = ScriptProtected.UNDEFINED;	
	private boolean structMerge = true;
	private static final URLItem[] empty = new URLItem[0];
	private static final Collection.Key REQUEST_TIMEOUT = KeyImpl.getInstance("RequestTimeout");
	private URLItem[] raw = empty;

	/**
	 * Standart Constructor
	 */
	public URLImpl() {
		super("url", SCOPE_URL, Struct.TYPE_LINKED);
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public void setEncoding(ApplicationContext ac, String encoding) throws UnsupportedEncodingException {
		encoding = encoding.trim().toUpperCase();
		if (encoding.equals(this.encoding)) return;
		this.encoding = encoding;
		structMerge = ac.getMmergeFormUrlAsStruct();
		if (isInitalized()) fillDecoded(raw, encoding, isScriptProtected(), ac.getSameFieldAsArray(SCOPE_URL), structMerge);
	}

	@Override
	public void initialize(PageContext pc) {
		if (encoding == null) encoding = pc.getWebCharset().name();
		if (scriptProtected == ScriptProtected.UNDEFINED) {
			scriptProtected = ((pc.getApplicationContext().getScriptProtect() & ApplicationContext.SCRIPT_PROTECT_URL) > 0) ? ScriptProtected.YES : ScriptProtected.NO;
			structMerge = pc.getApplicationContext().getMmergeFormUrlAsStruct();
		}		
		try {
			super.initialize(pc);
			raw = setFromQueryString(ReqRspUtil.getQueryString(pc.getHttpServletRequest()));

			fillDecoded(raw, encoding, isScriptProtected(), pc.getApplicationContext().getSameFieldAsArray(SCOPE_URL), structMerge);

			if (raw.length > 0 && pc.getConfig().isAllowURLRequestTimeout()) {
				Object o = get(REQUEST_TIMEOUT, null);
				if (o != null) {
					long timeout = Caster.toLongValue(o, -1);
					if (timeout != -1) pc.setRequestTimeout(timeout * 1000);
				}
				Caster.toDoubleValue(o, false, -1);
			}
		}
		catch (Exception e) {}
	}

	@Override
	public void reinitialize(ApplicationContext ac) {
		if (isInitalized()) {
			if (scriptProtected == ScriptProtected.UNDEFINED) {
				scriptProtected = ((ac.getScriptProtect() & ApplicationContext.SCRIPT_PROTECT_URL) > 0) ? ScriptProtected.YES : ScriptProtected.NO;
			}
			structMerge = ac.getMmergeFormUrlAsStruct();

			fillDecodedEL(raw, encoding, isScriptProtected(), ac.getSameFieldAsArray(SCOPE_URL), structMerge);
		}
	}

	@Override
	public void release(PageContext pc) {
		encoding = null;
		raw = empty;
		scriptProtected = ScriptProtected.UNDEFINED;
		super.release(pc);
	}

	@Override
	public void setScriptProtecting(ApplicationContext ac, boolean scriptProtected) {

		int _scriptProtected = scriptProtected ? ScriptProtected.YES : ScriptProtected.NO;
		// print.out(isInitalized()+"x"+(_scriptProtected+"!="+this.scriptProtected));
		if (isInitalized() && _scriptProtected != this.scriptProtected) {
			fillDecodedEL(raw, encoding, scriptProtected, ac.getSameFieldAsArray(SCOPE_URL), structMerge);
		}
		this.scriptProtected = _scriptProtected;
	}

	@Override
	public boolean isScriptProtected() {
		return scriptProtected == ScriptProtected.YES;
	}

	/**
	 * @return the raw
	 */
	public URLItem[] getRaw() {
		return raw;
	}
}