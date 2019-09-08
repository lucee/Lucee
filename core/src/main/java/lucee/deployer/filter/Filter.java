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
package lucee.deployer.filter;

import lucee.commons.io.res.Resource;

/**
 * Das Interface Filter dient der Deployer Klasse festzustellen, ob es sich bei einer Datei um eine
 * CFML Datei handelt oder nicht. Vergleichbar mit einem Filter bei Webserver zum zuteilen von
 * Dateien zu Modulen. Das Interface Filter ist als Interface implementiert um flexibler in dessen
 * Handhabung zu sein.
 */
public interface Filter {

	/**
	 * Gibt zurueck ob die eingegebene Datei eine CFML Datei ist oder nicht.
	 * 
	 * @param file File das geprueft werden soll.
	 * @return handelt es sich bei der CFML Datei um eine CFML
	 */
	public boolean isValid(Resource res);
}