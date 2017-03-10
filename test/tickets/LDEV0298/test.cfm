<cfscript>
	setting enablecfoutputonly="true";
	param name="FORM.Scene" default="1";
	errorMsg = "";
	try {
		if(FORM.Scene == 1){
			image function getImage1() {
				myObj = new image();
				return myObj;
			}
			getImage1();
		}else if(FORM.Scene == 2){
			image1 function getImage2() {
				myObj = new image1();
				return myObj;
			}
			getImage2();
		}else if(FORM.Scene == 3){
			inner.image function getImage3() {
				myObj = new inner.image();
				return myObj;
			}
			getImage3();
		}else if(FORM.Scene == 4){
			inner.image1 function getImage4() {
				myObj = new inner.image1();
				return myObj;
			}
			getImage4();
		}
	}catch(any e) {
		errorMsg = e.Message;
	}
	writeOutput(errorMsg);
</cfscript>