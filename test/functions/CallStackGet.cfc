<!--- 
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	

	public void function testCallStackGetArray(){
		var cs = _callStackGet( "array" );
		expect( cs[ 1 ].function ).toBeWithCase( "_callStackGet" );
		expect( cs[ 2 ].function ).toBeWithCase( "testCallStackGetArray" );
	}

	public void function testCallStackGetJSON(){
		var cs = DeserializeJson( _callStackGet( "json" ) );
		expect( cs[ 1 ].function ).toBeWithCase( "_callStackGet" );
		expect( cs[ 2 ].function ).toBeWithCase( "testCallStackGetJSON" );
	}

	// using a wrapper here so we aren't dependant on testbox internals
	private function _callStackGet( type ){
		return CallStackGet( type );
	}
} 
</cfscript>