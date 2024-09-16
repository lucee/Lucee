component {
	variables.fields=[];
	
	private function field(string displayName,string name,string defaultValue,boolean required,description,string type,string values) {
		return createObject("component","Field")
		.init(arguments.displayName,arguments.name,arguments.defaultValue,arguments.required,arguments.description,arguments.type,arguments.values);
	}

	private function group(string displayName,string description,numeric level=2) {
		return createObject("component","Group").init(arguments.displayName,arguments.description,arguments.level);
	}

	public function getCustomFields() {
		return variables.fields;
	}
}