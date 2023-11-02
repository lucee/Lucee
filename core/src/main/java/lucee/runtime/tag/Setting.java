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

import lucee.runtime.PageContextImpl;
import lucee.runtime.debug.DebuggerImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageExceptionImpl;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class Setting extends BodyTagImpl {

	private boolean hasBody;

	/**
	 * set the value requesttimeout
	 * 
	 * @param requesttimeout value to set
	 **/
	public void setRequesttimeout(double requesttimeout) {
		long rt;
		if (requesttimeout <= 0) rt = Long.MAX_VALUE;
		else rt = (long) (requesttimeout * 1000);

		pageContext.setRequestTimeout(rt);
	}

	/**
	 * set the value showdebugoutput Yes or No. When set to No, showDebugOutput suppresses debugging
	 * information that would otherwise display at the end of the generated page.Default is Yes.
	 * 
	 * @param showdebugoutput value to set
	 **/
	public void setShowdebugoutput(boolean showdebugoutput) {
		if (pageContext.getConfig().debug()) {
			DebuggerImpl d = (DebuggerImpl) pageContext.getDebugger();
			d.setOutput(showdebugoutput, ((PageContextImpl) pageContext).getListenSettings());
		}

	}

	public void setListen(boolean listen) {
		((PageContextImpl) pageContext).setListenSettings(true);
	}

	public void setInfo(String varName) throws PageException {
		Struct sct = new StructImpl();

		// debugging
		DebuggerImpl d = ((DebuggerImpl) pageContext.getDebugger());
		Struct debugging = new StructImpl();
		sct.set(KeyConstants._debugging, debugging);
		debugging.set(KeyConstants._status, d.getOutput());
		PageExceptionImpl pe = (PageExceptionImpl) d.getOutputContext();
		if (pe != null) {

			Array arr = pe.getTagContext(pageContext.getConfig());
			Iterator<Object> it = arr.valueIterator();
			StringBuilder sb = new StringBuilder();
			Struct tmp;
			while (it.hasNext()) {
				if (sb.length() > 0) sb.append('\n');
				tmp = Caster.toStruct(it.next());

				sb.append(tmp.get(KeyConstants._template) + ":" + tmp.get(KeyConstants._line));
			}
			debugging.set("location", sb.toString());
			// debugging.set("location", pe.getStackTraceAsString());
		}

		// request timeout
		Struct timeout = new StructImpl();
		sct.set(KeyConstants._timeout, timeout);
		timeout.set(KeyConstants._status, Caster.toDouble(pageContext.getRequestTimeout() / 1000));

		// enable cfoutput only
		Struct output = new StructImpl();
		sct.set(KeyConstants._output, output);
		short level = ((PageContextImpl) pageContext).getCFOutputOnly();
		output.set(KeyConstants._status, level > 0);
		output.set(KeyConstants._level, Caster.toDouble(level));

		// set variable
		pageContext.setVariable(varName, sct);
	}

	/**
	 * set the value enablecfoutputonly Yes or No. When set to Yes, cfsetting blocks output of HTML that
	 * resides outside cfoutput tags.
	 * 
	 * @param enablecfoutputonly value to set
	 * @throws PageException
	 **/
	public void setEnablecfoutputonly(Object enablecfoutputonly) throws PageException {
		if (enablecfoutputonly instanceof String && Caster.toString(enablecfoutputonly).trim().equalsIgnoreCase("reset")) {
			pageContext.setCFOutputOnly((short) 0);
		}
		else {
			pageContext.setCFOutputOnly(Caster.toBooleanValue(enablecfoutputonly));
		}
	}

	/**
	 * @deprecated this method is replaced by the method
	 *             <code>setEnablecfoutputonly(Object enablecfoutputonly)</code>
	 * @param enablecfoutputonly
	 */
	@Deprecated
	public void setEnablecfoutputonly(boolean enablecfoutputonly) {
		pageContext.setCFOutputOnly(enablecfoutputonly);
	}

	@Override
	public int doStartTag() {
		return EVAL_BODY_INCLUDE;
	}

	@Override
	public int doEndTag() {
		return EVAL_PAGE;
	}

	/**
	 * sets if tag has a body or not
	 * 
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {
		this.hasBody = hasBody;
	}

}