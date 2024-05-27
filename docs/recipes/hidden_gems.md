<!--
{
  "title": "Hidden Gems",
  "id": "hidden_gems",
  "description": "This document explains how to declare variables, function calls with dot and bracket notation, and passing arguments via URL/form scopes as an array.",
  "keywords": [
    "Hidden gems",
    "Declare variables",
    "Function calls",
    "Dot notation",
    "Bracket notation",
    "URL form scopes",
    "Array format",
    "Lucee"
  ]
}
-->
## Hidden Gems

This document explains how to declare variables, function calls with dot and bracket notation, and passing arguments via URL/form scopes as an array. These concepts are explained with simple examples below:

### Example 1: Declare Variables

// test.cfc

```luceescript
component {
	function getName() {
		return "Susi";
	}
}
```

// example1.cfm

```luceescript
function test() {
	var qry;
	dump(qry);
	query name="qry" datasource="test" {
		echo("select 1 as one");
	}
	dump(qry);
}
test();
```

In the cfm page, we have a test() function with a local variable scope assigned as an empty string `var qry`. When executing this cfm, the qry returns "1". Dumping the `qry` below the var declaration returns an empty string.

### Example 2: Dot and Bracket Notation for Function Calls

Lucee allows you to use bracket notation to call a component function.

// example2.cfm

```luceescript
// UDF call via dot notation
test = new Test();
dump( test.getName() );
// Dynamic function name
funcName = "getName";
dump(evaluate('test.#funcName#()'));
// UDF call via bracket notation
funcName = "getName";
dump( test[funcName]() );
```

These three different types of function calls are:

- Calling the user-defined function `getName()` from the component.
- Dynamic function name with evaluate function.
- User-defined function via bracket notation.

All three different function calls return the same content "Susi" as defined in the CFC page.

### Example 3: Passing Arguments via URL/Form Scopes as Array

Lucee allows passing URL and Form scope data as an array instead of a string list.

// example3.cfm

```lucee
<cfscript>
	dump(label:"URL", var:url);
	dump(label:"Form", var:form);
	// current name
	curr = listLast(getCurrentTemplatePath(),'\/');
</cfscript>

<cfoutput>
	<h1>Countries</h1>
	<form method="post" action="#curr#?country[]=USA&country[]=UAE">
		<pre>
			Countries Europe:	<input type="text" name="country[]" value="Switzerland,France,Germany" size="30">
			Countries America:	<input type="text" name="country[]" value="Canada,USA,Mexico" size="30">
			<input type="submit" name="send" value="send">
		</pre>
	</form>
</cfoutput>
```

// index.cfm

```luceescript
directory sort="name" action="list" directory=getDirectoryFromPath(getCurrentTemplatePath()) filter="example*.cfm" name="dir";
loop query=dir {
	echo('<a href="#dir.name#">#dir.name#</a><br>');
}
```

In this cfm page, URL and form scopes are available. The names are used twice.

- The query string on the URL scope has the same name `country` twice. Similarly, the form also has two fields with the same name `country`.
- Execute this cfm page in the browser & submit the form. It shows a single URL string list in merged format instead of two fields & Form fields also merged as a single `country` field.
- Adding square brackets behind the name `country[]` means it returns two separate strings in array format. You will see the difference in the browser while dumping that name with square brackets.

These simple methods are helpful for defining variables in different ways.

### Footnotes

Here you can see the above details in the video

[Lucee Hidden Gems](https://youtu.be/4MUKPiQv1kAsss)