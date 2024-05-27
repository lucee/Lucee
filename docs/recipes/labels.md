<!--
{
  "title": "Loop Labels",
  "id": "loop-labels",
  "keywords": [
    "loop",
    "label",
    "for",
    "while",
    "continue",
    "break"
  ]
}
-->
# Loop Labels

Lucee supports labels for all loop tags and statements, allowing you to control the flow of nested loops more precisely.

Labels are particularly useful when using the `break` or `continue` statements to avoid affecting only the nearest enclosing loop.

### Example

Consider the following examples:

### Tag-Based Example

In this example, the `break` statement is used with a label to exit the outer loop:

```run
<cfloop from="0" to="4" index="hour" label="outerLoop">
  <cfloop from="1" to="60" index="minute">
    <cfoutput>time: #hour#:#minute#<br></cfoutput>
    <cfbreak "outerLoop">
  </cfloop>
</cfloop>
```
Here, the `cfbreak` statement with the `outerLoop` label causes the loop to break out of the outer loop, not just the inner loop. As a result, only a single time value is printed.

### Script-Based Example

Similarly, you can use labels in script-based loops:

```run
<cfscript>
outerloop:for(hour=0; hour<=4; hour++) {
  for(minute=1; minute<=60; minute++) {
    echo("time: " & hour & ":" & minute & "<br>");
    continue outerloop;
  }
}
</cfscript>
```

In this example, the `continue outerloop` statement causes the loop to skip to the next iteration of the outer loop, effectively restarting the outer loop and ignoring the remaining iterations of the inner loop.

### Summary

Using labels with `break` and `continue` statements in Lucee allows you to control nested loops more effectively by specifying which loop to break out of or continue. This provides greater flexibility and clarity in managing complex looping structures.

If you have any questions or need further clarification, feel free to ask!
