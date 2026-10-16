component extends="BaseEntity" persistent="true" table="person" accessors="true" {
	property name="firstName" type="string";
	property name="lastName" type="string";
	// Override inherited property - should NOT be persisted to DB
	property name="modifiedDate" persistent="false";
}
