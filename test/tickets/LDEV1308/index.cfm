<cfscript>
	threads = "";
	arr = [1,2,3];
	try{
		arr.each(function(v){
			var t = "t#v#";
			threads = threads.listAppend(t);
			thread name=t {
				sleep(10);
			}
		});
		thread action="join" name=threads;
		result = threads;
	}
	catch(any e){
		result = e.message;
	}
	writeOutput(result);
</cfscript>