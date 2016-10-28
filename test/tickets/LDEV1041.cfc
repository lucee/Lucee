/*
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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
 */
 component extends="org.lucee.cfml.test.LuceeTestCase"	{
	

	public void function testSetMilliSecondMemberFunction(){
		var date=createDateTime(2000,1,1,0,0,0);
		assertEquals("{ts '2000-01-01 00:00:00'}",date);
		date.setMilliSecond(10);
		assertEquals("{ts '2000-01-01 00:00:00'}",date);

	}

	public void function testSetYearMemberFunction(){
		var date=createDateTime(2000,1,1,0,0,0);
		assertEquals("{ts '2000-01-01 00:00:00'}",date);
		date.setYear(2016);
		assertEquals("{ts '2016-01-01 00:00:00'}",date);

	}

	public void function testSetYear(){
		// this should not work
		var date=createDateTime(2000,1,1,0,0,0);
		var broken=false;
		try{
			setYear(date,2016);
		}
		catch(local.e){broken=true;}

		assertTrue(broken);
	}


} 



