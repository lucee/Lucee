<cfscript>
	param name="FORM.scene" default="1";

	if( form.scene eq 1 ){
		cf_test_fir = false;
		if( cf_test_fir ?: false == "false" ){
			test = "1";
		}
		writeoutput(test);
	}
	else if( form.scene eq 2 ){
		cf_test_sec = false;
		if( cf_test_sec ? true == "true" : false == "false" ){
			testOne = "2";
		}
		writeoutput(testOne);
	}
	else if( form.scene eq 3 ){
		cf_test_thr = true;
		if( cf_test_thr ? true : false){
			testTwo = "3";
		}
		writeoutput(testTwo);
	}
</cfscript>