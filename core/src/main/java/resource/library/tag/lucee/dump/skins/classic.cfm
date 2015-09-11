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
	// if you set this variable to true (default is false) the types of the simple values will not be displayed
	variables.bSuppressType = true;

	stClasses = { 
		stCustomClasses : { 
			Array:{headerColor:'090', darkColor:'cfc', lightColor:'fff', textColorHeader:'fff'},
			Mongo:{headerColor:'393', darkColor:'393', lightColor:'966' },
			Object:{headerColor:'f44', darkColor:'fcc', lightColor:'fff', textColorHeader:'fff' },
			Query:{headerColor:'a6a', darkColor:'fdf', lightColor:'fff', textColorHeader:'fff' },
			SimpleValue:{headerColor:'fff', darkColor:'fff', lightColor:'fff', pointer:0},
			Struct:{headerColor:'44c', darkColor:'cdf', lightColor:'fff', textColorHeader:'fff' , textColor:'000' },
			SubXML:{headerColor:'aaa', darkColor:'aaa', lightColor:'ddd' }, 
			XML:{headerColor:'aaa', darkColor:'aaa', lightColor:'ddd' },
			white: {headerColor:'fff', darkColor:'fff', lightColor:'ccc' }
		},
		stBaseClasses : {
			tdBase: {		style:'{border: 1px solid ##000;padding: 4px;vertical-align: top;}'},
			tableDump: {	style:'{font-family: Verdana,Geneva,Arial,Helvetica,sans-serif;font-size: xx-small;background-color: ##eee;color: ##000;border-spacing: 10px;border-collapse:collapse;empty-cells:show;}'},
			baseHeader: {	style:'{border: 2px solid ##000;padding: 4px;text-align: left;vertical-align: top;cursor:pointer;margin: 0px 0px -1px 0px;}'},
			header: {		style:'{font-weight: bold; border:1px solid ##000;}'},
			meta: {			style:'{font-weight: normal}'},
			tdClickName: { 	style:'{empty-cells: show}'}
		}
	};
</cfscript>