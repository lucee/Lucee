component extends="org.lucee.cfml.test.LuceeTestCase"	{


	public void function testNullConstantWhenEnabled(){
		var ns=getApplicationSettings().nullSupport;
		application action="update" NULLSupport=true;
		try {
			assertTrue(isNull(null));
		
			var x=null;
			assertTrue(isNull(x));
		}
		finally {
			application action="update" NULLSupport=ns;
		}
	}

	public void function testNullConstantWhenDisabled(){
		var ns=getApplicationSettings().nullSupport;
		application action="update" NULLSupport=false;
		try {
			//assertFalse(isNull(null));
			
			try{
				var x=null;
				var fails=false;
			}
			catch(e) {
				var fails=true;
			}
			
			assertTrue(fails);
		}
		finally {
			application action="update" NULLSupport=ns;
		}
	}

	public function _testNamedArgNull() {
		return structKeyList(arguments);
	}

	public void function testNamedArgNull() {
		var ns=getApplicationSettings().nullSupport;
		application action="update" NULLSupport=true;
		try {
			assertEquals('null',_testNamedArgNull(null:"1"));
			assertEquals('null',_testNamedArgNull(null="1"));
		}
		finally {
			application action="update" NULLSupport=ns;
		}
	}

	public void function testStructNameNull() {
		var ns=getApplicationSettings().nullSupport;
		application action="update" NULLSupport=true;
		try {
			null.x=1;
			assertTrue(isDefined("null.x"));
		}
		finally {
			application action="update" NULLSupport=ns;
		}
	}
} 

 