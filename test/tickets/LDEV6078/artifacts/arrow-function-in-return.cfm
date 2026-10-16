<cfscript>
// LDEV-6078: Arrow function in return statement
// Tests line number bug with inline arrow functions
// Bug: Arrow function body gets wrong line number

function executeWithCallback( required callback ) {
	return callback();
}

function processWithArrow() {
	return executeWithCallback(
		() => {
			var result = 1 + 2;
			return result;
		}
	);
}

systemOutput( processWithArrow(), true );
</cfscript>
