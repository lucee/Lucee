<cfscript>
	param name="form.scene" default="";
	testArray = ["one","two","three","four","five"];

	if(form.scene eq 1){
		try{
			t1 = testArray.append({}, true);
			writeOutput(serializeJson(isstruct(t1[6])));
		}
		catch(any e){ writeOutput(e.message); }
	}

	if(form.scene eq 2){
		t2 = testArray.append({1:"2"}, true);
		writeOutput(serializeJson(isstruct(t2[6])));
	}

	if(form.scene eq 3){
		t3 = testArray.append({"test":"lucee"}, true);
		writeOutput(serializeJson(isstruct(t3[6])));
	}

	if(form.scene eq 4){
		t4 = testArray.append({}, false);
		writeOutput(serializeJson(isstruct(t4[6])));
	}

	if(form.scene eq 5){
		t5 = testArray.append({"1":"2"}, false);
		writeOutput(serializeJson(isstruct(t5[6])));
	}

	if(form.scene eq 6){
		t6 = testArray.append({"lucee":"test"}, false);
		writeOutput(serializeJson(isstruct(t6[6])));
	}

</cfscript>
