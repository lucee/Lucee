component entityName="Author" table="Author2049" persistent=true accessors=true output=false {
	
	// Persistent Properties
	property name="authorID" ormtype="int" fieldtype="id" unsavedvalue="0" generator="increment";
	property name="authorName" ormtype="string";
	property name="createdDateTime" ormtype="timestamp";
	
	property name="primaryBook" cfc="Book" fieldType="many-to-one" fkcolumn="primaryBookID";
	
	property name="books" singularname="book" type="array" cfc="Book" fieldtype="one-to-many" cascade="all-delete-orphan" inverse="true" lazy="true";
	
	public void function preInsert() {
		setCreatedDateTime( now() );
	}
	
}