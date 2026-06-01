component {

	property name="evt" default="#nullValue()#";
	property name="label" default="#'hello-' & 'world'#";

	public string function evt(){
		return "from-method";
	}

	public boolean function isEvtNull(){
		return isNull( variables.evt );
	}

	public any function readEvtViaVariables(){
		return variables.evt ?: "absent-or-null";
	}

	public string function readLabelViaVariables(){
		return variables.label;
	}

	public string function diagnoseEvt(){
		if ( !structKeyExists( variables, "evt" ) ) return "absent";
		if ( isNull( variables.evt ) ) return "null-stored";
		if ( isCustomFunction( variables.evt ) ) return "udf-stomped";
		return "other:" & toString( variables.evt );
	}

}
