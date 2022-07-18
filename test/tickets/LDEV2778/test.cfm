<cfscript>
	param name="form.scene" default="";
	price=4.1;
	discount=25;
	if(form.scene eq 1){
		resultOne = price - ( price*discount/100 );
		resultRounded = Round( resultOne,2 );
		writeOutput(resultRounded);
	}
	if(form.scene eq 2){
		resultTwo = Round(3.075,2);				
		writeOutput(resultTwo);
	}
</cfscript>