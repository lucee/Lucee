<cfscript>
	param name="FORM.scene" default="";

	if (form.scene == 1) {
		silent {
			writeoutput("cfsilent inside");
		}
		writeoutput("cfsilent outside");
	}

	else if (form.scene == 2) {
		try {
			silent bufferoutput="true" {
				writeoutput("bufferoutput=true ");
				throw "error";
			}
		}
		catch(any e) {
			writeoutput(e.message);
		}
	}
	
	else if (form.scene == 3) {
		try {
			silent bufferoutput="false" {
				writeoutput("bufferoutput=false");
				throw "error";
			}
		}
		catch(any e) {
			writeoutput(e.message);
		}
	}

	else if (form.scene == 4) {
		// default bufferoutput must survive the pooled tag being reused
		for (i = 1; i <= 2; i++) {
			try {
				silent {
					writeoutput("default ");
					throw "error";
				}
			}
			catch(any e) {
				writeoutput(e.message & " ");
			}
		}
	}
</cfscript>