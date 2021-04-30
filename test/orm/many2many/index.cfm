<cfsetting showdebugoutput="no"><cfscript>
	

// manyToOne

	ml1=entityNew("moduleLang");
	//ml1.setId(1);
	ml1.setpModuleLang("de");
	//dump(ml1);
	
	ml2=entityNew("moduleLang");
	//ml2.setId(2);
	ml2.setpModuleLang("fr");
	//dump(ml2);
	

	module=entityNew("module");
	//module.setId(1);
	module.addModulelang(ml1);
	module.addModulelang(ml2);
	ml1.setModule(module);
	ml2.setModule(module);


	transaction {
		entitySave(ml1);
		entitySave(ml2);
		entitySave(module);
	}
	
	arr=entityLoad("moduleLang");
	//dump(arr);
	modules=entityLoad("module");
	module=modules[1];
	//dump(module);
	mls=module.getModulelang();
	//dump(mls);
	echo("moduleLangs:");
	mls.each(function(ml){
	   echo(ml.getId()&";");
	});

// ManyToMany
	b1=entityNew("Bookmark");
	b2=entityNew("Bookmark");
	//dump(b1);
	t1=entityNew("Tag");
	t2=entityNew("Tag");
	//dump(t1);

	b1.addTags(t1);
	b1.addTags(t2);
	b2.addTags(t1);
	b2.addTags(t2);
	
	/*t1.addBookmarks(b1);
	t1.addBookmarks(b2);
	t2.addBookmarks(b1);
	t2.addBookmarks(b2);*/
	transaction {
		entitySave(t1);
		entitySave(t2);
		entitySave(b1);
		entitySave(b2);
	}

	bookmarks=entityLoad("Bookmark");
	bookmark=bookmarks[1];
	tags=bookmark.getTags();
	echo("Tags:");
	tags.each(function(t){
	   echo(t.getId()&";");
	});


</cfscript>
