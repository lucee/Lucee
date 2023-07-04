component accessors="true" persistent="true" table="LDEV4121" {
	property name="id" type="string";
	property name="name" type="string" default="default organization name";
	
	function init() {
		return this;
	}
}