<cfscript>
	file = GetDirectoryFromPath(getcurrentTemplatepath())&'result.txt';
	thread action = "run" file="#file#" {
		try {
			sleep(50);
			testCFC = new sub.test();
			fileWrite(file, testCFC.test());
		}
		catch(any e) {
			fileWrite(file, e.message);
		}
	}
</cfscript>
