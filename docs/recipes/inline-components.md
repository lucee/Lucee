<!--
{
  "title": "Inline Component",
  "id": "inline-component",
  "since": "6.0",
  "description": "Learn how to create and use inline components in Lucee. This guide demonstrates how to define components directly within your CFML code, making it easier to create and use components without needing a separate .cfc file. Examples include creating an inline component and using it similarly to closures.",
  "keywords": [
    "CFML",
    "component",
    "inline-component",
    "Lucee"
  ]
}
-->
# Inline Component

Since Lucee 6.0, Lucee allows you to create inline components. These are components you can create directly in your CFML code, with no need to create a .cfc file for it. This feature allows you to directly use them, similar to closures.

This example shows how to create an inline component and then use it:

```run
<cfscript>
inline = new component {  
    function subTest() {
        return "inline<br>";
    } 
};  
dump("inline->" & inline.subTest());
dump(inline);
</cfscript>
```