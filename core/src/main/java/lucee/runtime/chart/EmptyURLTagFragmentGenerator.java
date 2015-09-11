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

import org.jfree.chart.imagemap.URLTagFragmentGenerator;

public class EmptyURLTagFragmentGenerator implements URLTagFragmentGenerator {

	/**
     * Generates a URL string to go in an HTML image map.
     * @param urlText  the URL text (fully escaped).
     * @return The formatted text
     */
    @Override
	public String generateURLFragment(String urlText) {
    	return "";
		//return " href=\"" + urlText + "\"";
	}

}