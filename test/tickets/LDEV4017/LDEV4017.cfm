<cfscript>
	try {
		transaction {
			person = entityLoadByPK("person","#form.uuid#");
		}

		writeoutput("#person.hasthoughts()# & #person.getThoughts()[1].getBody()#");
	}
	catch (any e) {
		writeoutput(e.stacktrace);
	}
</cfscript>