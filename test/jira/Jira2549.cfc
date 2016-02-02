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
	

	function testLeft() {

		assert( left("12345678", 2) == "12" );
		assert( left("12345678", 10) == "12345678" );

		assert( left("12345678", -2) == "123456" );

		assert( left("12345678", -8) == "12345678" );

		assert( left("12345678", -10) == "12345678" );

		assert( left("C:\Windows\Programs\", -1) == "C:\Windows\Programs" );
	}


	function testRight() {

		assert( right("12345678", 2) == "78" );
		assert( right("12345678", 10) == "12345678" );

		assert( right("12345678", -2) == "345678" );

		assert( right("12345678", -8) == "12345678" );

		assert( right("12345678", -10) == "12345678" );

		assert( right("C:\Windows\Programs\", -1) == ":\Windows\Programs\" );
	}


}