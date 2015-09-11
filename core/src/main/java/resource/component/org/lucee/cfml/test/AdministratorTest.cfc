

component extends="mxunit.framework.TestCase"	{

	public function setUp(){
		variables.admin=new org.lucee.cfml.Administrator("web","server");
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
		var locale='de_ch'
		
		admin.updateRegional(timezone,locale,timeserver,true);
		
		var mod=admin.getRegional();
		assertEquals(mod.locale,locale);
		assertEquals(mod.timeserver,timeserver);
		assertEquals(mod.timezone,timezone);
		assertEquals(mod.usetimeserver,true);
		
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
	
	
} 