component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	

	// TODO LDEV-6627: skipped until the OSGi extension exists, these load the janino bundles by bundle name which needs the OSGi service. Enable them (remove the skip) as soon as the OSGi extension is available.
	public void function testJanino() skip="true" {
   		var class=createObject("java","org.codehaus.janino.Compiler","org.lucee.janino");
	}
	public void function testJaninoCC() skip="true" {
		var class=createObject("java","org.codehaus.commons.compiler.Cookable","org.lucee.janinocc");
	}


} 