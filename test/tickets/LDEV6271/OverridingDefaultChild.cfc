component extends="InheritedDefaultParent" accessors="true" {

	property name="status" type="string" default="child-default";

	public string function status(){
		return "child-method-result";
	}

}
