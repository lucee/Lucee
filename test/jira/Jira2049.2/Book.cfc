component entityName="Book" table="Book2049b" persistent=true accessors=true output=false {

	// Persistent Properties
	property name="bookID" ormtype="int" fieldtype="id" unsavedvalue="0" generator="increment";
	property name="bookName" ormtype="string";
	
	// Related Persistent Properties
	property name="author" cfc="Author" fieldType="many-to-one" fkcolumn="authorID";
	
	////property name="authors" cfc="Author" singularName="author" fieldtype="one-to-many" fkcolumn="primaryBookID" type="array" inverse="true" lazy="true";

}