component extends="org.lucee.cfml.test.LuceeTestCase"	{


	public void function testLiteralStruct(){
		var sct={a:1,b:2};
		assertEquals('a,b',listSort(structKeyList(sct),'textNoCase'));
	}

	public void function testLiteralOrderedStruct(){
		var sct=[a:1,b:2];
		assertEquals('a,b',structKeyList(sct));
	}
	public void function testLiteralOrderedStructEmpty(){
		var sct=[:];
		assertEquals('',structKeyList(sct));
		var sct=[ : ];
		assertEquals('',structKeyList(sct));
		var sct=[=];
		assertEquals('',structKeyList(sct));
	}
	public void function testLiteralOrderedStruct(){
		var sct=[a:1,b:2];
		assertEquals('a,b',structKeyList(sct));
	}
	public void function testLiteralArray(){
		var arr=['a','b'];
		assertEquals('a,b',arrayToList(arr));
	}


	public void function testLiteralStructEvaluate(){
		var sct=evaluate('{a:1,b:2}');
		assertEquals('a,b',listSort(structKeyList(sct),'textNoCase'));
	}

	public void function testLiteralOrderedStructEvaluate(){
		var sct=evaluate('[a:1,b:2]');
		assertEquals('a,b',structKeyList(sct));
	}
	public void function testLiteralArrayEvaluate(){
		var arr=evaluate("['a','b']");
		assertEquals('a,b',arrayToList(arr));
	}
	public void function testLiteralOrderedStructEvaluateEmpty(){
		var sct=evaluate('[:]');
		assertEquals('',structKeyList(sct));
		var sct=evaluate('[ : ]');
		assertEquals('',structKeyList(sct));
		var sct=evaluate('[=]');
		assertEquals('',structKeyList(sct));
	}


} 



