<cfscript>
	test();
	private void function test(){
		try{
			var instance = new Person("lucee");
			res = instance.name;
		} catch( any e ){
			res = e.Message ;
		}
		writeOutput(res);
	}
</cfscript>