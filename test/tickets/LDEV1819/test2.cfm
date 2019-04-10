<cfscript>
    try {
    	test()
    }
    catch(any ex) {
    	writeOutput(ex.message)
    }

    private void function test()
    {
        var list = [33, 44, 55]
        list.each((value) => if(value < 0 || value > 100) throw(message = "Condition_False")) 
        writeOutput("Condition_True");
	}
</cfscript>