component accessors="true" persistent="true" table="child"{

	property name="ID" fieldtype="id" generator="assigned" unsavedValue="-1";
	
	function init(){
		return this;
	}

}