<cfscript>
	/**
	* 
	* This is a sample skin file for the Lucee DumpTemplate. You can copy this file into a new one in the same location and 
	* define the colors as you see fit. Just adapt the settings for the Header of an object to display, the key color and the 
	* value color. Next to it you can also define the text color for the header and the regular key and value elements
	* you shouldn't change the definition of the pointer for the SimpleValue though. This is in order not to display a mouse
	* pointer when hovering over a key.
	* The base classes define the single block elements of the dump. You should perhaps check the Developer Tools in your 
	* browser, if you want the find out the real hierarchy of the encapsulation.
	* 
	* The names for the classes are self explanatory.
	* The white element is used for an eval'ed variable or for References, if there is a circular reference
	* 
	* You can use all web colors you want. you just must not prefix them with a #
	* After any edit you must flush the component path cache in order to have the colors been picked up
	* 
	* */

	stClasses = { 
		stCustomClasses : {
			Array:{headerColor:'9c3', darkColor:'9c3', lightColor:'cf3' },
			Mongo:{headerColor:'393', darkColor:'393', lightColor:'966' },
			Object:{headerColor:'c99', darkColor:'c99', lightColor:'fcc' },
			Query:{headerColor:'c9c', darkColor:'c9c', lightColor:'fcf' },
			SimpleValue:{headerColor:'f60', darkColor:'f60', lightColor:'fc9', pointer:0 },
			Struct:{headerColor:'99f', darkColor:'99f', lightColor:'ccf' },
			SubXML:{headerColor:'996', darkColor:'996', lightColor:'cc9' }, 
			XML:{headerColor:'c99', darkColor:'c99', lightColor:'fff' },
			white: {headerColor:'fff', darkColor:'fff', lightColor:'ccc' },
			Component:{headerColor:'9c9', darkColor:'9c9', lightColor:'cfc'},
			PublicMethods:{headerColor:'fc9', darkColor:'fc9', lightColor:'ffc'},
			PrivateMethods:{headerColor:'fc3', darkColor:'fc3', lightColor:'f96'},
			Method:{headerColor:'c6f', darkColor:'c6f', lightColor:'fcf'}
		},
		stBaseClasses : {
			tdBase: {		style:'{border: 1px solid ##000;padding: 2px;vertical-align: top;}'},
			tableDump: {	style:'{font-family: Verdana,Geneva,Arial,Helvetica,sans-serif;font-size: 11px;background-color: ##eee;color: ##000;border-spacing: 1px;border-collapse:separate;}'},
			baseHeader: {	style:'{border: 1px solid ##000;padding: 2px;text-align: left;vertical-align: top;cursor:pointer;margin: 1px 1px 0px 1px;}'},
			header: {		style:'{font-weight: bold; border:1px solid ##000;}'},
			meta: {			style:'{font-weight: normal}'},
			tdClickName: { 	style:'{empty-cells: show}'}
		}
	};
</cfscript>