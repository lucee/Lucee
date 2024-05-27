<!--
{
  "title": "Convert a CFML Function/Component to use in Java",
  "id": "convert-cfml-to-java",
  "description": "Learn how to convert user-defined functions or components in Lucee to use them in Java. This guide demonstrates how to define components to implement Java interfaces, pass components to Java methods, explicitly define interfaces, and use the onMissingMethod feature. It also shows how to convert user-defined functions to Java lambdas.",
  "since": "6.0",
  "keywords": [
    "conversion",
    "cfc",
    "function",
    "component",
    "Java",
    "lambda",
    "Lucee"
  ]
}
-->
# Convert a CFML Function/Component to use in Java

Lucee allows you to convert user-defined functions or components so you can use them in Java.

## Component to Java Class

You simply add all functions defined for a Java interface to a component like this:

```lucee
// Component that implements all methods from interface CharSequence
component {
    function init(String str) {
        variables.str = reverse(arguments.str);
    }
    function length() {
        SystemOutput("MyString.length:" & str.length(), 1, 1);
        return str.length();
    }
    // ... more functions here
}
```

### Pass to Java

Then you can pass that component to a Java method needing a specific interface/class.

```lucee
// This class has a method that takes as an argument a CharSequence. 
// This way we can force Lucee to convert/wrap our component to that interface.
HashUtil = createObject("java", "lucee.commons.digest.HashUtil");

// This component implements all necessary functions for the CharSequence
cfc = new MyString("Susi Sorglos");

// Calling the method HashUtil.create64BitHashAsString(CharSequence cs) with our component as an argument
hash = HashUtil.create64BitHashAsString(cfc);
dump(hash);
```

### Explicit Definition and "onMissingMethod"

Of course, you can also define the interface you want to implement explicitly and you can use “onMissingMethod” so you do not have to implement every single function separately.

```lucee
component implementsJava="java.util.List" {
    function onMissingMethod(name, args) {
        if (name == "size") return 10;
        throw "method #name# is not supported!";
    }
}
```

## User-Defined Function to Java (as Lambda)

Functions get converted to a Lambda interface when the interface matches automatically. You can do the same with regular functions, but here the conversion happens when passing to Java.

```lucee
numeric function echoInt(numeric i) {
    if (i == 1) throw "Test output!!!";
    return i * 2;
}
```