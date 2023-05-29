<cfscript>
	param name = "FORM.scene" default = "";
	findAnimals = {
		cow : "moo",
		pig : "oink",
		cat : "meow"
	};
	if (form.scene == 1) {
		try{
			res = structFind(findAnimals, (key, value) => key == "cat" && value == "meow");
		}
		catch(any e){
			res = e.message;
		}
	}
	if (form.scene == 2) {
		try{
			res = findAnimals.find((key, value) => key == "cat" && value == "meow");
		}
		catch(any e) {
			res = e.message;
		}
	}
	writeOutput(res);
</cfscript>