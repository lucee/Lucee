<cfsetting showdebugoutput="no"><cfscript>
/*query {
	echo('DROP TABLE IF EXISTS Author2049b,Book2049b');
}*/
		newAuthor = entityNew("Author");

		/*
		Author.cfc has the following method
		public void function preInsert() {
			setAuthorName("Susix");
		}
		*/
		newAuthor.setAuthorName("Susi"); // so this should become "Susix"
		//newAuthor.setCreatedDateTime( now() );
		thisBook = entityNew("Book");
		thisBook.setBookName("book #now()#");
		
		thisBook.setAuthor( newAuthor );
		newAuthor.addBook( thisBook );
		
		newAuthor.setPrimaryBook( thisBook );
		
		entitySave( newAuthor );
		//newAuthor.setAuthorName("Susix");
		entitySave( thisBook );
		//writedump(newAuthor);
		//writedump(thisBook);
		ormFlush();

	query name="rtn.author" {
		echo('select * from Author2049b');
	}

	query name="rtn.book" {
		echo('select * from Book2049b');
	}
	echo(serialize(rtn));
</cfscript>