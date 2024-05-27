<!--
{
  "title": "File Extensions",
  "id": "file-extensions",
  "description": "Learn about the different file extensions supported by Lucee, including .cfm, .cfc, .cfml, and .cfs. This guide provides examples for each type of file.",
  "keywords": [
    "CFML",
    "cfm",
    "cfc",
    "cfml",
    "cfs",
    "file extensions"
  ]
}
-->
# File Extensions

Lucee supports several file extensions for different types of templates and components. The most common extensions are `.cfm`, `.cfc`, `.cfml`, and `.cfs`. Each serves a specific purpose in CFML development.

## .cfm

The `.cfm` extension is used for CFML templates. These files contain CFML code that is processed by the server to generate dynamic web pages.

### Example

```cfm
<!DOCTYPE html>
<html>
<head>
    <title>CFM Example</title>
</head>
<body>
    <cfset greeting = "Hello, World!">
    <cfoutput>
        <p>#greeting#</p>
    </cfoutput>
</body>
</html>
```

## .cfc

The `.cfc` extension is used for CFML Components (CFCs). These files define reusable components that encapsulate functionality in methods, similar to classes in object-oriented programming.

### Example

```cfc
component {
    public string function greet(string name) {
        return "Hello, " & name & "!";
    }
}
```

## .cfml

The `.cfml` extension is an alternative to `.cfm` and can be used for CFML templates. It serves the same purpose as `.cfm` but is less commonly used.

## .cfs

Since version 6.0, Lucee supports templates with the extension `.cfs`. These templates contain script code, similar to `.js` files in the JavaScript world. This allows you to write direct script code without the need for the `<cfscript>` tag.

### Example

```cfs
writeOutput("Hello from a .cfs file!");
```