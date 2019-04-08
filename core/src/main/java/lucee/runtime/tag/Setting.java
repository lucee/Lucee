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

import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.op.Caster;

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
	if (pageContext.getConfig().debug()) pageContext.getDebugger().setOutput(showdebugoutput);
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