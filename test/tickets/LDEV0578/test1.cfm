<cfscript>
	c = function(){
		thread name="threadTest"{
			thread.data = 100  + 200;
		}
	};
	c();
</cfscript>