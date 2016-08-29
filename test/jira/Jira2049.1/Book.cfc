component entityName="Book" table="Book2049" persistent=true accessors=true output=false {

	// Persistent Properties
	property name="bookID" ormtype="int" fieldtype="id" unsavedvalue="0" generator="increment";
	property name="bookName" ormtype="string";
	
	// Related Persistent Properties
	property name="author" cfc="Author" fieldType="many-to-one" fkcolumn="authorID";
	
}