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
			Array:{headerColor:'240', darkColor:'683', lightColor:'ff9', textColorHeader:'FFF'},
			SimpleValue:{headerColor:'036', darkColor:'036', lightColor:'05A', pointer:0, textColor:'FFF'},
			Struct:{headerColor:'730', darkColor:'C62', lightColor:'F95', textColorHeader:'FFF'},
			Query:{headerColor:'066', darkColor:'399', lightColor:'6BB', textColorHeader:'FFF'},
			XML:{headerColor:'024', darkColor:'47A', lightColor:'FFF', textColorHeader:'FFF'},
			SubXML:{headerColor:'730', darkColor:'C62', lightColor:'F95', textColorHeader:'FFF'}, 
			Object:{headerColor:'362', darkColor:'682', lightColor:'AE6', textColorHeader:'FFF'},
			Mongo:{headerColor:'393', darkColor:'393', lightColor:'966' },
			white: {headerColor:'FFF', darkColor:'FFF', lightColor:'ccc' },
			Component:{headerColor:'FC0', darkColor:'FE0', lightColor:'FF9'},
			PublicMethods:{headerColor:'511', darkColor:'FC9', lightColor:'FFE', textColorHeader:'FFF'},
			PrivateMethods:{headerColor:'222', darkColor:'666', lightColor:'EEE', textColorHeader:'FFF'},
			Method:{headerColor:'213', darkColor:'46B', lightColor:'5BF', textColorHeader:'FFF'}
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