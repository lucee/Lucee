component extends="MappedSuperClass" persistent="true" table="entity" accessors="true"{
	property name="ID" fieldType="id" type="numeric" generator="assigned" notnull="true";
	//don't want this super property to be persistent in this particular entity
	property name="superPersistentField" persistent="false";
}