component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	

	// TODO LDEV-6627: the OSGi extension cannot resolve org.lucee.janino, it is missing org.codehaus.commons.compiler (no bundle that exports that package is pulled in).
	public void function testJanino() skip="true" {
   		var class=createObject("java","org.codehaus.janino.Compiler","org.lucee.janino");
	}
	public void function testJaninoCC() {
		var class=createObject("java","org.codehaus.commons.compiler.Cookable","org.lucee.janinocc");
	}


} 