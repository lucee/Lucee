/*
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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
 */
 component extends="org.lucee.cfml.test.LuceeTestCase" {

 	public function testTimeFormatTimeZone_lz() localMode="modern" {

		dt=createDateTime(2000);
		org=getTimeZone();
		try{
			setTimeZone("UTC");
			assertEquals('UTC',timeFormat(dt,'z'));
			assertEquals('UTC',timeFormat(dt,'zz'));
			assertEquals('UTC',timeFormat(dt,'zzz'));

			assertEquals('Coordinated Universal Time',timeFormat(dt,'zzzz'));
			assertEquals('Coordinated Universal Time',timeFormat(dt,'zzzzz'));
			assertEquals('Coordinated Universal Time',timeFormat(dt,'zzzzzz'));

			setTimeZone("Europe/Zurich");
			assertEquals('CET',timeFormat(dt,'z'));
			assertEquals('CET',timeFormat(dt,'zz'));
			assertEquals('CET',timeFormat(dt,'zzz'));


			assertEquals('Central European Time',timeFormat(dt,'zzzz'));
			assertEquals('Central European Time',timeFormat(dt,'zzzzz'));
			assertEquals('Central European Time',timeFormat(dt,'zzzzzz'));
		}
		finally {
			setTimeZone(org);
		}
 	}

 	public function testMinute() localMode="modern" {
 		
		dt=createDateTime(2000,1,2,3,5,6,7);
		assertEquals('5',timeFormat(dt,'m'));
		assertEquals('5',timeFormat(dt,'M'));
		assertEquals('5',timeFormat(dt,'n'));
		assertEquals('5',timeFormat(dt,'N'));

		assertEquals('05',timeFormat(dt,'mm'));
		assertEquals('05',timeFormat(dt,'MM'));
		assertEquals('05',timeFormat(dt,'nn'));
		assertEquals('05',timeFormat(dt,'NN'));
		assertEquals('05',timeFormat(dt,'Nm'));
		assertEquals('05',timeFormat(dt,'Mn'));
 	}


 	public function testTimeFormatTimeZone_UZ() localMode="modern" {
 		
		dt=createDateTime(2000);
		org=getTimeZone();
		try{
			setTimeZone("UTC");
			assertEquals('+0000',timeFormat(dt,'Z'));
			assertEquals('+0000',timeFormat(dt,'ZZZZZ'));
			assertEquals('+0000',timeFormat(dt,'ZZZZZZZZ'));


			setTimeZone("Europe/Zurich");
			assertEquals('+0100',timeFormat(dt,'Z'));
			assertEquals('+0100',timeFormat(dt,'ZZZZZ'));
			assertEquals('+0100',timeFormat(dt,'ZZZZZZZZ'));
			dt=createDateTime(2000,7,7);
			assertEquals('+0200',timeFormat(dt,'ZZZZZZZZ'));
		}
		finally {
			setTimeZone(org);
		}

 	}

 	public function testTimeFormatTimeZone_X() localMode="modern" {
 		setTimeZone("CET");
 		
		dt=createDateTime(2000);
		org=getTimeZone();
		try{
			setTimeZone("UTC");
			assertEquals('Z',timeFormat(dt,'X'));
			assertEquals('Z',timeFormat(dt,'XX'));
			assertEquals('Z',timeFormat(dt,'XXX'));

			setTimeZone("Europe/Zurich");
			assertEquals('+01',timeFormat(dt,'X'));
			assertEquals('+0100',timeFormat(dt,'XX'));

			dt=createDateTime(2000,7,7);
			assertEquals('+02:00',timeFormat(dt,'XXX'));
		}
		finally {
			setTimeZone(org);
		}

 	}

 	public function testTimeFormatMember() localMode="modern" {
 		dt=CreateDateTime(2004,1,2,4,5,6);
		assertEquals("04",dt.timeFormat("hh"));
		
 	}
 	public function testTimeFormat() localMode="modern" {
 		setTimeZone("CET");
 		dt=CreateDateTime(2004,1,2,4,5,6);
		assertEquals("#timeFormat(dt,"hh:mm:ss")#", "04:05:06");
		assertEquals("#timeFormat(dt,"h:m:s")#", "4:5:6");
		assertEquals("#timeFormat(dt,"l")#", "0");
		assertEquals("#timeFormat(dt,"t")#", "A");
		assertEquals("#timeFormat(dt,"tt")#", "AM");
		
		dt=CreateDateTime(2004,1,2,11,59,59);
		assertEquals("#timeFormat(dt,"tt")#", "AM");
		
		dt=CreateDateTime(2004,1,2,12,0,0);
		assertEquals("#timeFormat(dt,"tt")#", "PM");
		
		dt=CreateDateTime(2004,1,2,14,0,0);
		assertEquals("#timeFormat(dt,"hh:HH:h:H")#", "02:14:2:14");

		assertEquals("#timeFormat(dt,"short")#x", "2:00 PMx");
		assertEquals("#timeFormat(dt,"medium")#x", "2:00:00 PMx");
		assertEquals("#timeFormat(dt,"long")#x", "2:00:00 PM CETx");
		assertEquals("#timeFormat(dt,"full")#x", "2:00:00 PM CETx");
		assertEquals("#timeFormat(dt)#x", "02:00 PMx"); 
		assertEquals("#timeFormat('')#", ""); 

		assertEquals("#timeFormat('','hh:mm')#x", "x");
		x='susi';
		try { 
			assertEquals("#timeFormat(x,'hh:mm')#x", "x");
		    fail("must throw:The value of the parameter 1, which is currently ""susi"", must be a class java.util.Date value. "); 
		}
		catch(local.e){}

		assertEquals("12:00 AMx" ,"#timeFormat(1)#x");
			
			

		d=CreateDateTime(2002,12,12,12,12,12);
		assertEquals("#timeFormat(d,"hh:mm:ss")#", "12:12:12");
		assertEquals("#timeFormat(d,"HH:mm:ss")#", "12:12:12");
		
		d=CreateDateTime(2002,12,12,13,12,12);
		assertEquals("#timeFormat(d,"hh:mm:ss")#", "01:12:12");
		assertEquals("#timeFormat(d,"HH:mm:ss")#", "13:12:12");


		date2=ParseDateTime("{ts '2008-09-01 01:34:55'}");
		date=ParseDateTime("{ts '2008-09-01 01:34:55.123'}");


		assertEquals("#Timeformat(date, "HHmmsslll")#", "013455123");
		assertEquals("#Timeformat(date, "HH")#", "01");
		assertEquals("#Timeformat(date, "mm")#", "34");
		assertEquals("#Timeformat(date, "ss")#", "55");
		assertEquals("#Timeformat(date, "lll")#", "123");
		assertEquals("#Timeformat(date2, "xlll")#", "x000");
		assertEquals("#Timeformat(date2, "xll")#", "x00");
		assertEquals("#Timeformat(date2, "xl")#", "x0");


		assertEquals("#DateFormat(date, "yymmdd") & Timeformat(date, "HHmmsslll")#", "080901013455123");



		assertEquals("#timeFormat("{t '12:15:00'}", 'h:mmt')#", "12:15P");


		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 28, 23, 15, 26),"h")#x", "11x");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 28, 23, 15, 26),"hh")#x", "11x");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 28, 23, 15, 26),"H")#x", "23x");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 28, 23, 15, 26),"HH")#x", "23x");

		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 0, 15, 26),"h")#x", "12x");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 0, 15, 26),"hh")#x", "12x");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 0, 15, 26),"H")#x", "0x");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 0, 15, 26),"HH")#x", "00x");

		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 1, 15, 26),"h")#x", "1x");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 1, 15, 26),"hh")#x", "01x");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 1, 15, 26),"H")#x", "1x");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 1, 15, 26),"HH")#x", "01x");

		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 12, 15, 26),"h")#x", "12x");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 12, 15, 26),"hh")#x", "12x");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 12, 15, 26),"H")#x", "12x");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 12, 15, 26),"HH")#x", "12x");

		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 0, 15, 26),"h TT")#", "12 AM");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 0, 15, 26),"hh TT")#", "12 AM");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 12, 15, 26),"h TT")#", "12 PM");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 12, 15, 26),"hh TT")#", "12 PM");


		// only supported by lucee
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 24, 0, 0),"h TT")#", "12 AM");
		assertEquals("#TimeFormat(CreateDateTime( 2009, 6, 29, 24, 0, 0),"hh TT")#", "12 AM");
		

		assertEquals("#timeFormat(0.95833332754629624306019763935182709246873855590,"HH:mm:ss:ll")#x", "22:59:59:999x");
		assertEquals("#timeFormat(0.9583333275462,"HH:mm:ss:ll")#x", "22:59:59:999x");
		assertEquals("#timeFormat(0.9583333275463,"HH:mm:ss:ll")#x", "23:00:00:00x");
		assertEquals("#timeFormat(0.958333327547,"HH:mm:ss:ll")#x", "23:00:00:00x");
		assertEquals("#timeFormat(0.95833332755,"HH:mm:ss:ll")#x", "23:00:00:00x");
		assertEquals("#timeFormat(0.9583333276,"HH:mm:ss:ll")#x", "23:00:00:00x");
		assertEquals("#timeFormat(0.95833333,"HH:mm:ss:ll")#x", "23:00:00:00x");

	}
}