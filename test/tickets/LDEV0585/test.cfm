<cfscript>
	param name="FORM.Scene" default="1";
	theImg = imageRead(expandPath("../../artifacts/image.jpg"));
	if( FORM.Scene == 1)
		imageResize(theImg,"128",""); // works
	else
		imageResize(theImg,"128"); // fails
	writeOutput(theImg.width);
</cfscript>