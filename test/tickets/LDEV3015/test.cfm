<cfscript>
	param name="form.scene" default="";

	if(form.scene eq 1){
		cfdocument(format="pdf", filename="#expandpath('pdf/withoutStyle.pdf')#", overwrite="true"){
			writeOutput("<p>This is PDF example document for the test without font styles.<p>");
		}
		cfpdf(action = "extracttext", type="xml", name="resultOne", source="pdf/withoutStyle.pdf");
		writeOutput(resultOne);
	}

	if(form.scene eq 2){
		cfdocument(format="pdf", filename="#expandpath('pdf/withStyle.pdf')#", overwrite="true"){
			writeOutput("<p>This is <strong>PDF example</strong> document for <small>the test</small> with font styles.</p>");
		}
		cfpdf(action = "extracttext", type="xml", name="resultTwo", source="pdf/withStyle.pdf");
		writeOutput(resultTwo);
	}
</cfscript>