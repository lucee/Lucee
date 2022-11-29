<cfparam name="FORM.scene" default="">
<cfscript>
	if( FORM.scene == 1 ) {
		try {
			res = isArray(entityLoad("test"));
		}
		catch(any e) {
			res = e.message;
		}
	}
	if( FORM.scene == 2 ) {
		try {
			res = isArray(entityLoad("test", {}));
		}
		catch(any e) {
			res = e.message;
		}
	}
	if( FORM.scene == 3 ) {
		try {
			res = isArray(entityLoad("test", {}, ""));
		}
		catch(any e) {
			res = e.message;
		}
	}
	if( FORM.scene == 4 ) {
		try {
			res = isArray(entityLoad("test", {}, "", {}));
		}
		catch(any e) {
			res = e.message;
		}
	}
	if( FORM.scene == 5 ) {
		try {
			res = isArray(entityLoad(name="test", id={}, unique="", options={}));
		}
		catch(any e) {
			res = e.message;
		}
	}
	if( FORM.scene == 6 ) {
		try {
			res = isArray(entityLoad(name="test", id={}, options={}));
		}
		catch(any e) {
			res = e.message;
		}
	}
	if( FORM.scene == 7 ) {
		try {
			res = isArray(entityLoad(name="test", id={}));
		}
		catch(any e) {
			res = e.message;
		}
	}
	if( FORM.scene == 8 ) {
		try {
			res = isArray(entityLoad(name="test", options={}));
		}
		catch(any e) {
			res = e.message;
		}
	}
	if( FORM.scene == 9 ) {
		try {
			res = isArray(entityLoad(name="test", unique=""));
		}
		catch(any e) {
			res = e.message;
		}
	}
	if( FORM.scene == 10 ) {
		try {
			res = isArray(entityLoad(name="test", id={}, unique=""));
		}
		catch(any e) {
			res = e.message;
		}
	}
	writeOutput(res);
</cfscript>