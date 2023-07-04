<cfscript>
	param name="FORM.scene" default="";
	try {
		if (form.scene == 1) {
			myArray = ['string']["Word1", "Word2"];
			writeoutput(serializeJSON(myArray));
		}
		else if (form.scene == 2) {
			myArray = arrayNew['numeric'](1);
			myArray.add(1);
			myArray.add(23);
			writeOutput(serializeJSON(myArray));
		}
	}
	catch(any e) {
		writeoutput(e.message);
	}
</cfscript>