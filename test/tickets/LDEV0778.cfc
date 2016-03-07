<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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



	str=chr(49)&chr(57)&chr(49)&chr(56)&chr(49)&chr(52)&chr(48)&chr(124)&chr(69)&chr(110)&chr(103)&chr(105)&chr(110)&chr(101)&chr(101)&chr(114)&chr(105)&chr(110)&chr(103)&chr(44)&chr(32)&chr(66)&chr(101)&chr(100)&chr(114)&chr(111)&chr(99)&chr(107)&chr(124)&chr(51)&chr(49)&chr(51)&chr(57)&chr(51)&chr(46)&chr(55)&chr(53)&chr(124)&chr(57)&chr(124)&chr(51)&chr(124)&chr(51)&chr(53)&chr(53)&chr(49)&chr(48)&chr(48)&chr(124)&chr(52)&chr(55)&chr(55)&chr(46)&chr(53)&chr(48)&chr(124)&chr(49)&chr(50)&chr(53)&chr(57)&chr(46)&chr(55)&chr(53)&chr(124)&chr(52)&chr(46)&chr(48)&chr(48)&chr(13)&chr(10)&chr(49)&chr(57)&chr(52)&chr(53)&chr(49)&chr(53)&chr(52)&chr(124)&chr(77)&chr(117)&chr(108)&chr(104)&chr(111)&chr(108)&chr(108)&chr(97)&chr(110)&chr(100)&chr(44)&chr(32)&chr(71)&chr(97)&chr(98)&chr(114)&chr(105)&chr(101)&chr(108)&chr(108)&chr(97)&chr(124)&chr(51)&chr(48)&chr(53)&chr(50)&chr(53)&chr(46)&chr(48)&chr(48)&chr(124)&chr(49)&chr(50)&chr(124)&chr(51)&chr(124)&chr(49)&chr(52)&chr(51)&chr(51)&chr(54)&chr(52)&chr(48)&chr(124)&chr(50)&chr(52)&chr(54)&chr(46)&chr(48)&chr(48)&chr(124)&chr(49)&chr(50)&chr(50)&chr(53)&chr(46)&chr(48)&chr(48)&chr(124)&chr(52)&chr(46)&chr(48)&chr(48)&chr(13)&chr(10)&chr(50)&chr(48)&chr(48)&chr(51)&chr(54)&chr(48)&chr(52)&chr(124)&chr(82)&chr(101)&chr(101)&chr(115)&chr(44)&chr(32)&chr(75)&chr(97)&chr(116)&chr(104)&chr(114)&chr(121)&chr(110)&chr(124)&chr(50)&chr(50)&chr(51)&chr(57)&chr(50)&chr(46)&chr(50)&chr(50)&chr(124)&chr(49)&chr(50)&chr(124)&chr(51)&chr(124)&chr(54)&chr(53)&chr(55)&chr(50)&chr(57)&chr(55)&chr(124)&chr(49)&chr(48)&chr(52)&chr(46)&chr(49)&chr(53)&chr(124)&chr(49)&chr(48)&chr(49)&chr(50)&chr(46)&chr(49)&chr(53)&chr(124)&chr(52)&chr(46)&chr(53)&chr(48)&chr(13)&chr(10)&chr(49)&chr(48)&chr(48)&chr(51)&chr(52)&chr(56)&chr(55)&chr(124)&chr(83)&chr(117)&chr(44)&chr(32)&chr(89)&chr(117)&chr(110)&chr(32)&chr(67)&chr(104)&chr(101)&chr(110)&chr(103)&chr(124)&chr(50)&chr(48)&chr(55)&chr(57)&chr(48)&chr(46)&chr(48)&chr(48)&chr(124)&chr(56)&chr(124)&chr(52)&chr(124)&chr(54)&chr(56)&chr(50)&chr(49)&chr(124)&chr(50)&chr(48)&chr(56)&chr(57)&chr(46)&chr(48)&chr(48)&chr(124)&chr(50)&chr(48)&chr(56)&chr(57)&chr(46)&chr(48)&chr(48)&chr(124)&chr(49)&chr(48)&chr(46)&chr(48)&chr(48)&chr(13)&chr(10)&chr(51)&chr(52)&chr(55)&chr(56)&chr(54)&chr(57)&chr(124)&chr(68)&chr(105)&chr(120)&chr(111)&chr(110)&chr(44)&chr(32)&chr(68)&chr(111)&chr(114)&chr(105)&chr(115)&chr(32)&chr(67)&chr(124)&chr(49)&chr(57)&chr(50)&chr(53)&chr(56)&chr(46)&chr(53)&chr(48)&chr(124)&chr(49)&chr(51)&chr(124)&chr(50)&chr(124)&chr(50)&chr(57)&chr(54)&chr(56)&chr(56)&chr(50)&chr(124)&chr(55)&chr(55)&chr(52)&chr(46)&chr(51)&chr(52)&chr(124)&chr(55)&chr(55)&chr(52)&chr(46)&chr(51)&chr(52)&chr(124)&chr(52)&chr(46)&chr(48)&chr(48)&chr(13)&chr(10);
	CSVParser=createObject("java","lucee.runtime.text.csv.CSVParser");

	public void function testWhiteSpaceAtTheEnd(){
		var qry=CSVParser.toQuery(str, '|', '"', nullValue(), false );
		assertEquals(5,qry.recordcount);
		assertEquals(9,qry.columnCount);
	}
} 
</cfscript>