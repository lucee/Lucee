/**
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"	{

	variables.CR = chr(13);

	function testNamedParams() {

		var q1 = query( name: ['Igal', 'Micha', 'Gert', 'Mark'], country: ['US', 'CH', 'CH', 'UK'] );

		var params = { name: { value: 'Micha' } };

		query name="local.q2" dbtype="query" params=params { echo("

			SELECT 	*
			FROM 	q1
			WHERE	name = :name#variables.CR#
		") };
	}
	
}