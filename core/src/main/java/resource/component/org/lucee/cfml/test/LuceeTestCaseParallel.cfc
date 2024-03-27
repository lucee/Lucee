component extends="org.lucee.cfml.test.LuceeTestCase"  {


	private function parallel(title, threadCount=1, repetitition=1, body)  {
		if(arguments.threadcount<1 || arguments.threadcount>1000) {
			throw "thread count need to be a number between 1 and 1000, now it is [#arguments.threadcount#]";
		}
		if(arguments.repetitition<1 || arguments.repetitition>1000) {
			throw "repetitition need to be a number between 1 and 1000, now it is [#arguments.repetitition#]";
		}
		if(arguments.threadcount==1 || arguments.repetitition==1) {
			throw "repetitition or thread count need to be bigger than 1";
		}
		var prefix=createUniqueID();
		var exceptions = [];    
		for (var i = 1; i <= arguments.repetitition; i++) {
			var names = [];  
			for (var y = 1; y <= arguments.threadcount; y++) {
				var name="testThread-#prefix#:#i#:#y#";
				arrayAppend(names, name);
				thread action="run" name=name title=arguments.title body=arguments.body exceptions=exceptions {
					try {
						it(title,body);
					}
					catch(e) {
						arrayAppend(exceptions, e);
					}
				}
			}
			thread action="join" name=arrayToList(names); 
		}
	}
}