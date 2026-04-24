component accessors="true" {

	property name="myFlag" type="boolean" default="true";
	property name="label" type="string" default="hello";

	public BuilderComponent function myFlag( boolean state=true ){
		variables.myFlag = arguments.state;
		return this;
	}

	public string function readFlagViaVariables(){
		return variables.myFlag;
	}

	public boolean function useFlagAsBoolean(){
		return variables.myFlag;
	}

	public string function readLabelViaVariables(){
		return variables.label;
	}

}
