<cfscript>
	param default="" name="url.job" type="string";
	param default=-1 name="url.mapping_id" type="numeric";

	mappings=getPageContext().getConfig().getMappings();

	if ( url.job == "save" && url.mapping_id > -1 ) {
		mappings=getPageContext().getConfig().getMappings();
		mappings[url.mapping_id].physical = form.physical;
		mappings[url.mapping_id].virtual  = form.virtual;
		mappings[url.mapping_id].archive  = form.archive;
		if ( isDefined("form.trusted") ) {
			mappings[url.mapping_id].trusted  = True;
		} else {
			mappings[url.mapping_id].trusted  = False;
		}
	}
	
	if ( url.job == stText.Buttons.Delete ) {
		tmp = ArrayDeleteAt(mappings, url.mapping_id);
	} else if ( url.job == "add" ) {
		tmp = ArrayAppend(mappings, StructNew());
		url.mapping_id = ArrayLen(mappings);

		mappings[url.mapping_id].physical = form.physical;
		mappings[url.mapping_id].virtual  = form.virtual;
		mappings[url.mapping_id].archive  = form.archive;
		if ( isDefined("form.trusted") ) {
			mappings[url.mapping_id].trusted  = True;
		} else {
			mappings[url.mapping_id].trusted  = False;
		}
	}
</cfscript>
