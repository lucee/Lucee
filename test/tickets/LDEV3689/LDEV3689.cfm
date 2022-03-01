<cfparam name = "Form.scene" default = "">
<cfscript>
	file = GetDirectoryFromPath(getcurrentTemplatepath())&'result.txt';
	name = createUniqueID();
	thread action = "run" name="#name#" file="#file#" scene="#form.scene#" {
		sleep(50);
		try {
			if (scene == 1) {
				var y = 55;
				savecontent variable="x" {
					cf_dollar(value = y);
				}
				FileWrite( file, x);
			}
			else if (scene == 2) {
				include "test.cfm";
			}
		}
		catch(any e) {
			FileWrite( file, e.message);
		}
	} 
</cfscript>
