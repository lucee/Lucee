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
package lucee.runtime.chart;

import lucee.commons.lang.HTMLEntities;

import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;

public class ToolTipTagFragmentGeneratorImpl implements
		ToolTipTagFragmentGenerator {

	private String url;
	
	public ToolTipTagFragmentGeneratorImpl(String url) {
		this.url=url;
	}	

	/**
     * Generates a tooltip string to go in an HTML image map.
     * @param toolTipText  the tooltip.
     * @return The formatted HTML area tag attribute(s).
     */
    @Override
	public String generateToolTipFragment(String toolTipText) {
    	toolTipText=HTMLEntities.escapeHTML(toolTipText,HTMLEntities.HTMLV20);
    	
    	String href="";
    	
        return href+" title=\"" + toolTipText 
            + "\" alt=\"" + toolTipText 
            + "\"";
    }

}