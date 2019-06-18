component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}
	
	//public function setUp(){}

	private int function echoInt(int i) type="java" {
		if(i==1)throw new Exception("shit happens!!!");
		 return i*2;
	}
	private String function to_string(String str1, String str2) type="java" {
		 return new java.lang.StringBuilder(str1).append(str2).toString();
	}

	public void function testUDF(){
		assertEquals(20,echoInt(10));
		assertTrue(isInstanceOf(echoInt,"java.util.function.IntUnaryOperator"));
		assertEquals("Hello Susi",to_string("Hello"," Susi"));
	}

	public void function testClosure(){
		var c= function (int x) returntype="int"  type="java" {
			x++;
			int y=x;
			return y;
		};
		assertEquals(11,c(10));
	}


} 