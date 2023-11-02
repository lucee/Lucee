<cfscript>
	param name = "FORM.Scene" default="";

	testFun(2204,"testcase");
	function testFun(number,text){
		if (form.scene eq 1) {
			StructEach(deserializeJson(serializeJson(arguments)),function(key, value){ 
				writeoutput("key: " & key & " value: " & value & ";"); 
			});
		}	
		else if(form.scene eq 2){
			StructEach(arguments,function(key, value){ 
				writeoutput("key: " & key & " value: " & value & ";"); 
			});
		}
	}	
</cfscript>
