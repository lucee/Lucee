component accessors="true" {

	property name="status" type="string" default="parent-default";
	property name="counter" type="numeric" default="42";

	public string function readStatusViaVariables(){
		return variables.status;
	}

	public any function readCounterViaVariables(){
		return variables.counter;
	}

	public boolean function isStatusUdf(){
		return isCustomFunction( variables.status );
	}

}
