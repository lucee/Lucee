<cfsetting showdebugoutput="no">
<cfscript>
	// we load the jar as OSGi bundle from remote, so it is visible
	ConsoleReader = createObject(type:"java", classname:"jline.console.ConsoleReader",bundleName:'jline',bundleVersion:'2.12.0');
   	completor = new completor(); 
	jCompletor = createDynamicProxy( completor , [ 'jline.console.completer.Completer' ] );
   	echo(jCompletor.complete("", 1, []));
</cfscript>