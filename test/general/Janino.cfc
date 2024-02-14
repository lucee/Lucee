component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	

	public void function testJanino(){
   		var class=createObject("java","org.codehaus.janino.Compiler","org.lucee.janino");
	}
	public void function testJaninoCC(){
		var class=createObject("java","org.codehaus.commons.compiler.Cookable","org.lucee.janinocc");
	}


} 