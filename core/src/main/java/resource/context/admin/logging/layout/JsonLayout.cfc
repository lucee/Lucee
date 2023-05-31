component  extends="Layout" {
	fields=array(
		field("Enviroment Variables","envnames","",false,"A comma separated list of enviroment variable names you would like to include in the log entry","textarea")
		,field("Compact","compact","no",false,"If it is set to ""yes"", the appender does not use end-of-lines and indentation.","radio","yes,no")
		,field("Complete","complete","no",false,"If it is set to ""no"", the appender does not write the JSON open array character ""["" 
			at the start of the document, ""]"" and the end, nor comma "","" between records.","radio","yes,no")
		,field("Location Info","locationInfo","no",false,"If it is set to ""no"", this means there will be no location information output by this layout. 
			If the the option is set to ""yes"", then the file name and line number of the statement at the origin of the log statement will be output.","radio","yes,no")
		,field("Properties","properties","no",false,"Sets whether MDC key-value pairs should be output.","radio","yes,no")
		,field("Include Time","includeTimeMillis","no",false,"If it is set to ""yes"",  the timeMillis attribute is included in the Json payload instead of the instant.
			timeMillis will contain the number of milliseconds since midnight, January 1, 1970 UTC.","radio","yes,no")
		,field("Charset","charset","UTF-8",false,"Charset used to write the file","text")
		
		,group("Stacktrace","Complete Java Stacktrace")
		,field("Include Stacktrace","includestacktrace","yes",false,"Include the Java Stacktrace in the log entry or not.","radio","yes,no")
		,field("Stacktrace as String","stacktraceAsString","yes",false,"Set the Java Stacktrace as a single string or as an array of structs","radio","yes,no")
		
	);

	public string function getClass() {
		return "lucee.commons.io.log.log4j2.layout.JsonLayout";
	}

	public string function getLabel() {
		return "Json";
	}

	public string function getDescription() {
		return "The output of the Json Layout consists of a series of structure entries. 
		It does not output a complete well-formed Json until the file is full. ";
	}
}