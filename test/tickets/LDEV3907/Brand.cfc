component displayname="brand" entityname="brand" table="brand" persistent=true output=false accessors=true {
	// Persistent Properties
	property name="brandID" ormtype="string" length="32" fieldtype="id" generator="uuid" unsavedvalue="" default="1";
	property name="activeFlag" ormtype="boolean";
	property name="urlTitle" ormtype="string";
	property name="brandName" ormtype="string";
}