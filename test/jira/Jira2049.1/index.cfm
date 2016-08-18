<cfsetting showdebugoutput="no">
<cfoutput>

	<cfscript>
		newAuthor = entityNew("Author");
		newAuthor.setAuthorName("author #now()#");
		
		thisBook = entityNew("Book");
		thisBook.setBookName("book #now()#");
		
		thisBook.setAuthor( newAuthor );
		newAuthor.addBook( thisBook );
		
		newAuthor.setPrimaryBook( thisBook );
		
		entitySave( newAuthor );
		entitySave( thisBook );
		//writedump(newAuthor);
		//writedump(thisBook);
		//writedump(ormGetSession().getEntityMode());
		//writedump(ormGetSession().getEntityMode().toString());
		ormFlush();
		
	</cfscript>

</cfoutput>