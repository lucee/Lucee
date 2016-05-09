component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}


	public void function testBase(){
		var base =LDEV0835.Base::getData();
		var a =LDEV0835.A::getData();
		var b =LDEV0835.B::getData();
		assertEquals("Base:Base",base);
	}
	public void function testA(){
		var base =LDEV0835.Base::getData();
		var a =LDEV0835.A::getData();
		var b =LDEV0835.B::getData();
		assertEquals("A:A",a);
	}
	public void function testB(){
		var base =LDEV0835.Base::getData();
		var a =LDEV0835.A::getData();
		var b =LDEV0835.B::getData();
		assertEquals("B:Base",b);
	}


} 



