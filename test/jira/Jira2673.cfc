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
component extends="org.lucee.cfml.test.LuceeTestCase"	{


	function testISO8601() {

		var dt = createDateTime(2013, 12, 24, 1, 22, 33);

		var dtiso8601 = dateTimeFormat( dt, "ISO8601" );

		var date = listFirst( dtiso8601, 'T' );
		var time = listLast(  dtiso8601, 'T' );

		var dateParts = listToArray( date, '-' );

		assert( len( dateParts[1] ) == 4 );
		assert( len( dateParts[2] ) == 2 );
		assert( len( dateParts[3] ) == 2 );

		var timeParts = listToArray( listFirst( time, 'Zz+-' ), ':' );

		assert( len( timeParts[1] ) == 2 );		
		assert( len( timeParts[2] ) == 2 );		
		assert( len( timeParts[3] ) == 2 );		
	}
	
}