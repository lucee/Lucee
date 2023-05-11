<cfscript>
	param name="FORM.Scene" default="";
	
 	image=ImageNew("", 50,50,"","red");
    addBorder=5;
    w = 250 + (addBorder *2);
    h = 250 + (addBorder *2);;
    transparentBorder = ImageNew(source="", width=w, height=h, Type="argb",canvasColor="green");

    if( form.scene EQ 1 ){	
	   	transparentBorder.paste(image,addBorder,addBorder);
	   	writeOutput("Success");
    }

    if( form.scene EQ 2 ){
	   	try{ 		
	   		transparentBorder.paste(image2=image, x=addBorder, y=addBorder);
			writeOutput("Success");
	   	}
	   	catch (any e){
	   		writeOutput(e.message);
	   	}
	}
</cfscript>