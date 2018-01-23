<cfscript>
	errorMsg = "";
	try {
		if(FORM.Scene == 1){
			myStr=StructNew("ordered","text","asc",false);
			myStr.zz = {age=26, department="IT"};
	        myStr.xx = {age=31, department="Accounting"};
	        myStr.yy = {age=30, department="Audit"};
			res1 = myStr.keyList();
			writeOutput(res1);
		}else if(FORM.Scene == 2){
			myStr=StructNew("ordered","text","desc",false);
			myStr.zz = {age=26, department="IT"};
	        myStr.xx = {age=31, department="Accounting"};
	        myStr.yy = {age=30, department="Audit"};
			res2 = myStr.keyList();
			writeOutput(res2);
		}else if(FORM.Scene == 3){
			myNumb=StructNew("ordered","numeric","asc",false);
			myNumb.3 = {age=26, department="IT"};
	        myNumb.1 = {age=31, department="Accounting"};
	        myNumb.2 = {age=30, department="Audit"};
			res3 = myNumb.keyList();
			writeOutput(res3);
		}else if(FORM.Scene == 4){
			myNumb=StructNew("ordered","numeric","desc",false);
			myNumb.3 = {age=26, department="IT"};
	        myNumb.1 = {age=31, department="Accounting"};
	        myNumb.2 = {age=30, department="Audit"};
			res4 = myNumb.keyList();
			writeOutput(res4);
		}
	}catch(any e) {
		errorMsg = e.Message;
	}
	writeOutput(errorMsg);
</cfscript>