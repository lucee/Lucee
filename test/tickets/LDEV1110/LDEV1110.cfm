<cfscript>
	try{
		cfinvoke(webservice="http://soaptest.parasoft.com/calculator.wsdl", method="add", returnvariable="answer"){
			cfinvokeargument(name="x", value="6");
			cfinvokeargument(name="y", value="3");
		}
	}catch(any e){
		answer = e.Message;
	}
	writeOutput(answer);
</cfscript>