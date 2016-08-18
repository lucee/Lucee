<cfsetting showdebugoutput="no">
<cfoutput>

	<cfscript>
		newAuthor = entityNew("Author");
		newAuthor.setAuthorName("author #now()#");
		
		thisBook = entityNew("Book");
		thisBook.setBookName("book #now()#");
		
		thisBook.setAuthor( newAuthor );
		newAuthor.addBook( thisBook );
		
		
		thisBook2 = entityNew("Book");
		newAuthor.setPrimaryBook( thisBook2 );
		
		entitySave( newAuthor );
		entitySave( thisBook );
		entitySave( thisBook2 );
		//writedump(newAuthor);
		//writedump(thisBook);
		
		
		ormFlush();
		
	</cfscript>
</cfoutput>


<cfquery>
select * from Author2049b
</cfquery>
<cfquery>
select * from Book2049b
</cfquery>