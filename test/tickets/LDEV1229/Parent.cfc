component accessors="true" persistent="true" table="parent"{

	property name="ID" fieldtype="id" generator="assigned" unsavedValue="-1";

	property name="children" singularName="child" fieldType="one-to-many" cfc="Child" fkColumn="parentID" inverse="true" cascade="all-delete-orphan";

	function init(){
		return this;
	}
}