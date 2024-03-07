<cfscript>
	param name = "url.scene" default = "";
	switch(url.scene){
		case "good":
			echo( new test4642().whoDoYouLove() );
			break;
		case "bad":
			try {
				fileWrite("test4642.cfc", "component { I love adobe!");
				new test4642(); // will throw, bad cfc
			} catch (e) {
				// expected to fail
			}
			// can't repo yet
			echo( new test4642().whoDoYouLove() ); // should be broken, faithfully returns lucee from cache
			break;
		default:
			throw "wuts?";
	}
</cfscript>