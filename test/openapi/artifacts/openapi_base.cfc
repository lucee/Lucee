component 
	displayname="OpenAPI Test Service" 
	hint="A comprehensive test service demonstrating all OpenAPI data types and patterns"
	returnFormat="json" {

	private function onError(exception e)
		access="remote"
		returntype="object"
		hint="Handles errors for the API"
		httpmethod="POST" {

		return {
			error: true,
			message: e.message
		};
	}

	remote function getMetadata()
		access="remote" 
		returntype="object" 
		hint="Returns a getMetadata() for this cfc"
		httpmethod="GET" {
		
		return getMetadata(this);
	}

	remote function getName(required string name)
		access="remote" 
		returntype="object" 
		hint="Returns a string"
		httpmethod="GET" {
		
		return { "name": arguments.name };
	}

	remote string function getArrayAsString(required array arr)
		access="remote" 
		returntype="object" 
		hint="Returns an array joined as string"
		httpmethod="GET" {
		
		return { "result": ArrayToList(arguments.arr) };
	}

	remote string function getNumberAsCurrency(required numeric price)
		access="remote" 
		returntype="object" 
		hint="Returns an array joined as string"
		httpmethod="GET" {
		
		return { "price": dollarFormat(arguments.price) };
	}

	remote string function getStructAsString(required struct s)
		access="remote" 
		returntype="object" 
		hint="Returns a struct as string"
		httpmethod="GET" {

		return { "result": arguments.s.toJson() };
	}

	remote function getDateFormatted(date date)
		access="remote" 
		returntype="object" 
		hint="Returns a formatted date string"
		httpmethod="GET" {

		return { "result": dateFormat(arguments.date, "yyyy-mm-dd") };
	}

	remote function getBooleanAsYesNo(boolean value)
		access="remote" 
		returntype="object" 
		hint="Returns a boolean as yes /  no"
		httpmethod="GET" {

		return { "result": yesNoFormat(arguments.value) };
	}

	remote function getWithDefaults(string name="Lucee",
			number version="7",
			boolean rocks=true)
		access="remote" 
		returntype="object" 
		hint="Returns a arguments"
		httpmethod="GET" {
		
		return {"arguments": arguments};
	}


}