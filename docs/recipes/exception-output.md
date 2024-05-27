<!--
{
  "title": "Exception Output",
  "id": "exception-output",
  "related": [
    "tag-catch",
    "tag-rethrow",
    "tag-throw",
    "tag-try"
  ],
  "categories": [
    "debugging"
  ],
  "description": "How to catch and display exceptions.",
  "keywords": [
    "Exception",
    "Output",
    "Catch",
    "Display exceptions",
    "Lucee",
    "try-catch"
  ]
}
-->
# Output Exceptions

How to catch and display exceptions

## Example

```run
<cfscript>
try {
  throw "an error happened";
}
catch ( any e ){
  dump(e);
}
</cfscript>
Go on with your code
```

 [tag-Dump] shows the full exception structure without blocking your code. Dump includes all stack trace with it.

## Example 2

```lucee
<cfscript>
try {
  throw "an error happened again!";
}
catch ( any e ){
  echo(e);
}
</cfscript>
Go on with your code
```

Here we simply echo the exception. It shows the normal exception without blocking your code.

[https://www.youtube.com/watch?v=vM-4R2A-ZsM](https://www.youtube.com/watch?v=vM-4R2A-ZsM)