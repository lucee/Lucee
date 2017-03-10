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

	public function setUp(){
		variables.admin=new org.lucee.cfml.Administrator("web",request.WEBADMINPASSWORD);
	}
	
	public void function testGetRegional(){
		var reginal=admin.getRegional();
		assertEquals(isStruct(reginal),true);
		assertEquals(listSort(structKeyList(reginal),'textnocase'),'locale,timeserver,timezone,usetimeserver');
	}
	
	public void function testUpdateRegional(string timezone, string locale,string timeserver,boolean usetimeserver){
		var org=admin.getRegional();
		var timeserver='swisstime.ethz.ch';
		var timezone='gmt';
		var locale='german (swiss)'
		
		admin.updateRegional(timezone,locale,timeserver,true);
		
		var mod=admin.getRegional();
		var l=GetLocaleInfo(mod.locale,'en_US');
		assertEquals("de",l.language);
		assertEquals("CH",l.country);
		assertEquals(timeserver,mod.timeserver);
		assertEquals(timezone,mod.timezone);
		assertEquals(true,mod.usetimeserver);
		
		// without optional arguments
		admin.updateRegional(timezone,locale,timeserver);
		admin.updateRegional(timezone,locale);
		admin.updateRegional(timezone);
		admin.updateRegional();
		
		// reset
		admin.updateRegional(org.timezone,org.locale,org.timeserver,org.usetimeserver);
		
		
	}
	
	public void function testResetRegional(){
		var org=admin.getRegional();
		admin.resetRegional();
		
		// reset
		admin.updateRegional(org.timezone,org.locale,org.timeserver,org.usetimeserver);
	
	}
	
	public void function testGetCharset(){
		var charset=admin.getCharset();
		assertEquals(isStruct(charset),true);
		assertEquals(listSort(structKeyList(charset),'textnocase'),'jreCharset,resourceCharset,templateCharset,webCharset');

	}
	
	public void function testGetAvailableTimeZones(){
		var timezones=admin.getAvailableTimeZones();
		assertEquals(isQuery(timezones),true);
		assertEquals(listSort(timezones.columnlist,'textnocase'),'DISPLAY,ID');
	}
	
	/**
	* @hint returns all available locales
	*/
	public void function testGetAvailableLocales(){
		var locales=admin.getAvailableLocales();
		assertEquals(isStruct(locales),true);
	}
	
	public void function testUpdateCharset(string resourceCharset, string templateCharset,string webCharset){
		var org=admin.getCharset();
		var resourceCharset='utf-8';
		var templateCharset='utf-8';
		var webCharset='utf-8';
		
		admin.updateCharset(resourceCharset,templateCharset,webCharset);
		
		var mod=admin.getCharset();
		assertEquals(mod.resourceCharset,resourceCharset);
		assertEquals(mod.templateCharset,templateCharset);
		assertEquals(mod.webCharset,webCharset);
		// without optional arguments
		admin.updateCharset(resourceCharset,templateCharset);
		admin.updateCharset(resourceCharset);
		admin.updateCharset();
		
		// reset
		admin.updateCharset(org.resourceCharset,org.templateCharset,org.webCharset);
		
	}
	
	public void function testResetCharset(){
		var org=admin.getCharset();
		admin.resetCharset();
		
		// reset
		admin.updateCharset(org.resourceCharset,org.templateCharset,org.webCharset);
	
	}
	
	public void function testGetCharset(){
		var charset=admin.getCharset();
		assertEquals(isStruct(charset),true);
		assertEquals(listSort(structKeyList(charset),'textnocase'),'jreCharset,resourceCharset,templateCharset,webCharset');

	}
	
	
	public void function testUpdateJar(){
		//dump(getmetaData(admin));abort;
		//admin.updateJar();
	}
	
}