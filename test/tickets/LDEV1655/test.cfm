<cfscript>
	errorMsg = "";
	try {
		myStr = StructNew();
		myStr.cc="one";
		myStr.aa="three";
		myStr.bb="four";

		myNumb = StructNew();
		myNumb.3="one";
		myNumb.2="two";
		myNumb.1="three";

		if(FORM.Scene == 1){
			myStr = StructToSorted(myStr,"text","asc",false);
			res1 = myStr.keyList();
			writeOutput(res1);
		}else if(FORM.Scene == 2){
			myStr=StructToSorted(myStr,"text","desc",false);
			res2 = myStr.keyList();
			writeOutput(res2);
		}else if(FORM.Scene == 3){
			myNumb=StructToSorted(myNumb,"numeric","asc",false);
			res3 = myNumb.keyList();
			writeOutput(res3);
		}else if(FORM.Scene == 4){
			myNumb=StructToSorted(myNumb,"numeric","desc",false);
			res4 = myNumb.keyList();
			writeOutput(res4);
		}else if(FORM.Scene == 5){
			myStr=mystr.ToSorted("text","asc",false);
			res5 = myStr.keyList();
			writeOutput(res5);
		}else if(FORM.Scene == 6){
			myStr=mystr.ToSorted("text","desc",false);
			res6 = myStr.keyList();
			writeOutput(res6);
		}else if(FORM.Scene == 7){
			myNumb=myNumb.ToSorted("numeric","asc",false);
			res7 = myNumb.keyList();
			writeOutput(res7);
		}else if(FORM.Scene == 8){
			myNumb=myNumb.ToSorted("numeric","desc",false);
			res8 = myNumb.keyList();
			writeOutput(res8);
		}
	}catch(any e) {
		errorMsg = e.Message;
	}
	writeOutput(errorMsg);
</cfscript>