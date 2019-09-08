component {
	fields=array();

	private component function field(required string displayName, required string name, string defaultValue="", boolean required="no", any description="", string type="text", string values="") output=false {
		return createObject("component","Field").init(	arguments.displayName,
															arguments.name,
															arguments.defaultValue,
															arguments.required,
															arguments.description,
															arguments.type,
															arguments.values);
	}

	private component function group(required string displayName, string description="", numeric level="2") output=false {
		return createObject("component","Group").init(arguments.displayName,arguments.description,arguments.level);
	}

	public array function getCustomFields() {
		return fields;
	}

	public void function onBeforeUpdate(required struct custom) output=false {
	}

	public void function onBeforeError(required struct cfcatch) output=false {
	}

	public string function getId() {
		throw( message="implement function getID():string" );
	}

	public string function output(required struct custom, required struct debugging) {
		throw( message="implement function output():string" );
	}

	function isEnabled( custom, key ) {
		return structKeyExists( arguments.custom, arguments.key ) 
			&& ( arguments.custom[ arguments.key ] == "Enabled" 
				|| arguments.custom[ arguments.key ] == "true" 
		);
	}
}
