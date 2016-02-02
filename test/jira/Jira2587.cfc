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

	public function setUp(){
		setTimeZone("CET");
		date=createDateTime(2009,6,9,14,30,3);
	}
	
	public void function testA(){
		// Am/pm marker
		assertEquals("PM",DateTimeFormat(date,"a"));
		assertEquals("PM",DateTimeFormat(date,"aa"));
		assertEquals("PM",DateTimeFormat(date,"aaa"));
		assertEquals("PM",DateTimeFormat(date,"aaaa"));
		assertEquals("PM",DateTimeFormat(date,"aaaaa"));
		
		assertEquals("A",DateTimeFormat(date,"A"));
	}
	
	public void function testD(){
		// Day in month
		assertEquals("9",DateTimeFormat(date,"d"));
		assertEquals("09",DateTimeFormat(date,"dd"));
		assertEquals("009",DateTimeFormat(date,"ddd"));
		assertEquals("0009",DateTimeFormat(date,"dddd"));
		assertEquals("00009",DateTimeFormat(date,"ddddd"));
		
		// Day in year
		assertEquals("160",DateTimeFormat(date,"D"));
		assertEquals("160",DateTimeFormat(date,"DD"));
		assertEquals("160",DateTimeFormat(date,"DDD"));
		assertEquals("0160",DateTimeFormat(date,"DDDD"));
		assertEquals("00160",DateTimeFormat(date,"DDDDD"));
	}
	
	public void function testE(){
		// Day of week
		assertEquals("Tue",DateTimeFormat(date,"E"));
		assertEquals("Tue",DateTimeFormat(date,"EE"));
		assertEquals("Tue",DateTimeFormat(date,"EEE"));
		assertEquals("Tuesday",DateTimeFormat(date,"EEEE"));
		assertEquals("Tuesday",DateTimeFormat(date,"EEEEE"));
		
		assertEquals("e",DateTimeFormat(date,"e"));
	}
	
	public void function testF(){
		// Day of week in month
		assertEquals("2",DateTimeFormat(date,"F"));
		assertEquals("02",DateTimeFormat(date,"FF"));
		assertEquals("002",DateTimeFormat(date,"FFF"));
		assertEquals("0002",DateTimeFormat(date,"FFFF"));
		assertEquals("00002",DateTimeFormat(date,"FFFFF"));
		
		assertEquals("f",DateTimeFormat(date,"f"));
	}

	public void function testG(){
		// Era designator
		assertEquals("AD",DateTimeFormat(date,"G"));
		assertEquals("AD",DateTimeFormat(date,"GG"));
		assertEquals("AD",DateTimeFormat(date,"GGG"));
		assertEquals("AD AD",DateTimeFormat(date,"GG GG"));
		
		
		assertEquals("g",DateTimeFormat(date,"g"));
		
	}
	
	public void function testH(){
		// Hour in day (0-23)
		assertEquals("14",DateTimeFormat(date,"H"));
		assertEquals("14",DateTimeFormat(date,"HH"));
		assertEquals("014",DateTimeFormat(date,"HHH"));
		assertEquals("0014",DateTimeFormat(date,"HHHH"));
		assertEquals("00014",DateTimeFormat(date,"HHHHH"));
		
		// Hour in am/pm (1-12)
		assertEquals("2",DateTimeFormat(date,"h"));
		assertEquals("02",DateTimeFormat(date,"hh"));
		assertEquals("002",DateTimeFormat(date,"hhh"));
		assertEquals("0002",DateTimeFormat(date,"hhhh"));
		assertEquals("00002",DateTimeFormat(date,"hhhhh"));
	}
	
	public void function testK(){
		// Hour in day (1-24)
		assertEquals("14",DateTimeFormat(date,"k"));
		assertEquals("14",DateTimeFormat(date,"kk"));
		assertEquals("014",DateTimeFormat(date,"kkk"));
		assertEquals("0014",DateTimeFormat(date,"kkkk"));
		assertEquals("00014",DateTimeFormat(date,"kkkkk"));
		
		// Hour in am/pm (0-11)
		assertEquals("2",DateTimeFormat(date,"K"));
		assertEquals("02",DateTimeFormat(date,"KK"));
		assertEquals("002",DateTimeFormat(date,"KKK"));
		assertEquals("0002",DateTimeFormat(date,"KKKK"));
		assertEquals("00002",DateTimeFormat(date,"KKKKK"));
	}
	
	public void function testL(){
		// milli seconds
		assertEquals("0",DateTimeFormat(date,"l"));
		assertEquals("00",DateTimeFormat(date,"ll"));
		assertEquals("000",DateTimeFormat(date,"lll"));
		assertEquals("0000",DateTimeFormat(date,"llll"));
		assertEquals("00000",DateTimeFormat(date,"lllll"));
		
		assertEquals("0",DateTimeFormat(date,"L"));
		assertEquals("00",DateTimeFormat(date,"LL"));
		assertEquals("000",DateTimeFormat(date,"LLL"));
		assertEquals("0000",DateTimeFormat(date,"LLLL"));
		assertEquals("00000",DateTimeFormat(date,"LLLLL"));
		
	}
	
	public void function testM(){
		// month
		assertEquals("6",DateTimeFormat(date,"M"));
		assertEquals("06",DateTimeFormat(date,"MM"));
		assertEquals("Jun",DateTimeFormat(date,"MMM"));
		assertEquals("June",DateTimeFormat(date,"MMMM"));
		assertEquals("June",DateTimeFormat(date,"MMMMM"));
		
		assertEquals("6",DateTimeFormat(date,"m"));
		assertEquals("06",DateTimeFormat(date,"mm"));
		assertEquals("Jun",DateTimeFormat(date,"mmm"));
		assertEquals("June",DateTimeFormat(date,"mmmm"));
		assertEquals("June",DateTimeFormat(date,"mmmmm"));
	}
	
	public void function testN(){
		// MINUTES IN HOUR
		assertEquals("30",DateTimeFormat(date,"n"));
		assertEquals("30",DateTimeFormat(date,"nn"));
		assertEquals("030",DateTimeFormat(date,"nnn"));
		assertEquals("0030",DateTimeFormat(date,"nnnn"));
		assertEquals("00030",DateTimeFormat(date,"nnnnn"));
		
		assertEquals("30",DateTimeFormat(date,"N"));
		assertEquals("30",DateTimeFormat(date,"NN"));
		assertEquals("030",DateTimeFormat(date,"NNN"));
		assertEquals("0030",DateTimeFormat(date,"NNNN"));
		assertEquals("00030",DateTimeFormat(date,"NNNNN"));
	}
	
	public void function testS(){
		// Second in minute
		assertEquals("3",DateTimeFormat(date,"s"));
		assertEquals("03",DateTimeFormat(date,"ss"));
		assertEquals("003",DateTimeFormat(date,"sss"));
		assertEquals("0003",DateTimeFormat(date,"ssss"));
		assertEquals("00003",DateTimeFormat(date,"sssss"));
		
		assertEquals("3",DateTimeFormat(date,"S"));
		assertEquals("03",DateTimeFormat(date,"SS"));
		assertEquals("003",DateTimeFormat(date,"SSS"));
		assertEquals("0003",DateTimeFormat(date,"SSSS"));
		assertEquals("00003",DateTimeFormat(date,"SSSSS"));
	}
	
	public void function testT(){
		// 
		assertEquals("P",DateTimeFormat(date,"t"));
		assertEquals("PM",DateTimeFormat(date,"tt"));
		assertEquals("PM",DateTimeFormat(date,"ttt"));
		assertEquals("PM",DateTimeFormat(date,"tttt"));
		assertEquals("PM",DateTimeFormat(date,"ttttt"));
		
		assertEquals("P",DateTimeFormat(date,"T"));
		assertEquals("PM",DateTimeFormat(date,"TT"));
		assertEquals("PM",DateTimeFormat(date,"TTT"));
		assertEquals("PM",DateTimeFormat(date,"TTTT"));
		assertEquals("PM",DateTimeFormat(date,"TTTTT"));
		
	}
	
	public void function testW(){
		// Week in year
		assertEquals("24",DateTimeFormat(date,"w"));
		assertEquals("24",DateTimeFormat(date,"ww"));
		assertEquals("024",DateTimeFormat(date,"www"));
		assertEquals("0024",DateTimeFormat(date,"wwww"));
		assertEquals("00024",DateTimeFormat(date,"wwwww"));
		
		// Week in month
		assertEquals("2",DateTimeFormat(date,"W"));
		assertEquals("02",DateTimeFormat(date,"WW"));
		assertEquals("002",DateTimeFormat(date,"WWW"));
		assertEquals("0002",DateTimeFormat(date,"WWWW"));
		assertEquals("00002",DateTimeFormat(date,"WWWWW"));
	}
	
	public void function testY(){
		// year
		assertEquals("2009",DateTimeFormat(date,"y"));
		assertEquals("09",DateTimeFormat(date,"yy"));
		assertEquals("2009",DateTimeFormat(date,"yyy"));
		assertEquals("2009",DateTimeFormat(date,"yyyy"));
		assertEquals("02009",DateTimeFormat(date,"yyyyy"));
		
		assertEquals("Y",DateTimeFormat(date,"Y"));
			
	}
	
	public void function testZ(){
		
		// Second in minute
		assertEquals("CEST",DateTimeFormat(date,"z"));
		assertEquals("CEST",DateTimeFormat(date,"zz"));
		assertEquals("CEST",DateTimeFormat(date,"zzz"));
		assertEquals("Central European Summer Time",DateTimeFormat(date,"zzzz"));
		assertEquals("Central European Summer Time",DateTimeFormat(date,"zzzzz"));
		
		assertEquals("+0200",DateTimeFormat(date,"Z"));
		assertEquals("+0200",DateTimeFormat(date,"ZZ"));
		assertEquals("+0200",DateTimeFormat(date,"ZZZ"));
		assertEquals("+0200",DateTimeFormat(date,"ZZZZ"));
		assertEquals("+0200",DateTimeFormat(date,"ZZZZZ"));
	}
	
	public void function testPredefined(){
		assertEquals("6/9/09 2:30 PM",DateTimeFormat(date,"short"));
		assertEquals("Jun 9, 2009 2:30:03 PM",DateTimeFormat(date,"medium"));
		assertEquals("June 9, 2009 2:30:03 PM CEST",DateTimeFormat(date,"long"));
		assertEquals("Tuesday, June 9, 2009 2:30:03 PM CEST",DateTimeFormat(date,"full"));
		assertEquals("09-Jun-2009 14:30:03",DateTimeFormat(date));
		
	}
	public void function testSpecialCharacter(){
		assertEquals("'",DateTimeFormat(date,"''"));
		assertEquals("3 t 3",DateTimeFormat(date,"s 't' s"));
		
	}
}
</cfscript>