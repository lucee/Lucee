component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	private void function test() localmode=true { 
		fileName = 'foo-' & createUUID() & '.cfm';
		fileWrite( fileName, '<cfscript>function foo() { return "bar"; }</cfscript>' );
		//cfc = new MyCFC( fileName );
		include fileName;
        this.foo = variables.foo;
		fileDelete( fileName );
		sleep(10); //make sure we have a new timestamp
    	try{
    		include fileName;
    		fail("include above shold fail");
    	}catch(e) {}// fails because the file no longer exist, but because of that lucee removes the template from pool
    
		assertEquals("barx",foo());
	}

} 


    
    


