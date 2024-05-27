<!--
{
  "title": "Function SystemOutput",
  "id": "systemoutput_function",
  "description": "This document explains the systemoutput function with some simple examples.",
  "keywords": [
    "SystemOutput function",
    "Debugging",
    "Output stream",
    "Error stream",
    "Stack trace"
  ]
}
-->
# Function SystemOutput #

This document explains the systemoutput function with some simple examples.

## Example 1: ##

```luceescript
<cfscript>
directory action="list" directory=getDirectoryFromPath(getCurrentTemplatePath()) filter="example*.cfm" name="dir";
loop query=dir {
	echo('<a href="#dir.name#">#dir.name#</a><br>');
}
</cfscript>
```


```lucee
<cfscript>
dump(cgi); // or writedump if you love to write more
echo(now()); // or writeoutput if you love to write more
</cfscript>
```

This example has a simple dump with CGI. It displays normally in the browser while we are running example1.cfm.

## Example 2: ##

```luceescript
// example2.cfm
systemOutput(now(),true); // with new line
```

systemOutput() creates output content in the web browser (console). Here the systemoutput has two arguments: first argument _now()_ for current date and time, and second argument _true_ for a new line. Run this in the browser and see the content in the console.

## Example 3: ##

```luceescript
// example3.cfm
systemOutput(now(),true,true); // send to error stream
```

This example uses three arguments: first argument `now()` for current date and time, second argument `true` for a new line, and third argument for the stream. The stream argument indicates which stream the output should go. There are two streams to choose from: "Output stream" and "Error stream". A value of true in the third argument indicates the output should go to the error stream. Run this in the browser and see the contents with the output stream in the console.

## Example 4: ##

```luceescript
// example4.cfm
systemOutput(cgi,true); // complex object
```

In addition to simple strings or simple values, you can also pass complex objects to the SystemOutput() function. In this example we pass CGI as the argument. When you run this in the browser, you get a serialized output in the console.

## Example 5: ##

```luceescript
// example5.cfm
systemOutput("Here we are:<print-stack-trace>",true); // complex object
```

SystemOutput() has another good feature too. There is `<print-stack-trace>` used to show helpful information (where it is, which template, on which line number) while we mouse over the dump content. Lucee will detect and show the stack-trace if we add `<print-stack-trace>` to the SystemOutput() function in our code. When we run this in the browser, we see the stack-trace in the console.

## Footnotes ##

Here you can see these details on video also:

[Function SystemOutput](https://www.youtube.com/watch?v=X_BQPFPD320)