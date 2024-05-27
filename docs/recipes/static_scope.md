<!--
{
  "title": "Static scope in components",
  "id": "static_scope",
  "categories": [
    "component",
    "scopes",
    "static"
  ],
  "description": "Static scope in components is needed to create an instance of cfc and call its method.",
  "keywords": [
    "Static scope",
    "Components",
    "Lucee",
    "Application scope",
    "Server scope",
    "GetComponentMetaData"
  ]
}
-->
## Static scope in components

Static scope in components is needed to create an instance of cfc and call its method. It is used to avoid creating an instance each time you use the cfc.

You can create an object in the Application init() function, and make it at application scope, so you can directly call the methods.

We explain this methodology with a simple example below:

### Example:

```luceescript
// index.cfm
directory sort="name" action="list" directory=getDirectoryFromPath(getCurrentTemplatePath()) filter="example*.cfm" name="dir";
loop query=dir {
    echo('<a href="#dir.name#">#dir.name#</a><br>');
}
```

1) Create a constructor of the component. It is the instance of the current path and also create new function hey().

```luceescript
// Example0.cfc
Component {
    public function init() {
        dump("create an instance of " & listLast(getCurrentTemplatePath(),'\/'));
    }
    public function hey() {
        dump("Salve!");
    }
}
```

2) Next, we instantiate the component four times, and then call the hey() function. Run this example00.cfm page in the browser. It shows five dumps. Four dumps coming from inside of the constructor and the fifth dump is from hey(). Note that the init() function is private, so you cannot load it from outside the component. Therefore, you have no access to the message within init() from the cfscript in the example below.

```luceescript
// example0.cfm
new Example0();
new Example0();
new Example0();
cfc = new Example0();
cfc.hey();
```

### Example 1:

As our code gets more complicated, we need to make some additions to it.

* One option is to create the Component in the application scope or server scope, or to use the function GetComponentMetaData to store components in a more persistent manner.

The static scope for components was introduced in Lucee 5.0. This is a scope that is shared with all instances of the same component.

Here is an example showing the use of static scope:

```luceescript
// Example1.cfc
Component {
    static var counter = 0;
    public function init() {
        static.counter++;
        dump("create an instance of " & listLast(getCurrentTemplatePath(),'\/') & " " & static.counter);
    }
    public function getCount() {
        return static.counter;
    }
}
```

Here, the variable `counter` is defined in the static scope. This means that all instances of Example1.cfc share this variable.

2) In the following example, we call the Example1() function three times. Each time, the `counter` variable is incremented and shared across all instances.

```luceescript
// example1.cfm
new Example1();
new Example1();
new Example1();
```

### Example 2:

1) Another example is using the static scope to store the result of a time-consuming operation that does not need to be recomputed every time.

```luceescript
// Example2.cfc
Component {
    static var data = [];
    public function init() {
        if (arrayLen(static.data) == 0) {
            for (i = 1; i <= 100; i++) {
                arrayAppend(static.data, i * i);
            }
        }
        dump(static.data);
    }
}
```

Here, the array `data` is defined in the static scope, which means it will be computed only once and shared across all instances.

2) In the following example, we call the Example2() function twice. The array `data` is computed only once and reused in the second instance.

```luceescript
// example2.cfm
new Example2();
new Example2();
```

### Example 3:

1) The static scope can also be used for functions. In this example, we define a static function that is available to all instances.

```luceescript
// Example3.cfc
Component {
    public static function hello() {
        return "Hello, World!";
    }
}
```

2) In the following example, we call the static function `hello` without creating an instance of Example3.

```luceescript
// example3.cfm
dump(Example3::hello());
```

### Example 4:

1) The static scope can be used to count the number of instances created from a component.

```luceescript
// Example4.cfc
Component {
    static var counter = 0;
    public function init() {
        static.counter++;
        dump(static.counter & " instances used so far");
    }
}
```

2) In the following example, we call the Example4() function five times. Each time the function is called, the count of `counter` in the static scope increases.

```luceescript
// example4.cfm
new Example4();
new Example4();
new Example4();
new Example4();
new Example4();
```

### Example 5:

1) We can also use the static scope to store constant data like HOST, PORT.

* If we store the instance in the variable scope, you will run into problems when you have a thousand components or it gets loaded a thousand times. This is a waste of time and memory storage.
* The static scope means that a variable only exists once and is independent of how many instances you have. So it is more memory efficient to do it that way. You can also do the same for functions.

```luceescript
// Example5.cfc
Component {
    static {
        static.HOST = "lucee.org";
        static.PORT = 8080;
    }
    public static function splitFullName(required string fullName) {
        var arr = listToArray(fullName, " 	");
        return {'lastname': arr[1], 'firstname': arr[2]};
    }
    public function init(required string fullName) {
        variables.fullname = static.splitFullName(fullName);
    }
    public string function getLastName() {
        return variables.fullname.lastname;
    }
}
```

2) In the following example, we call the Example5() function in two ways. It has a function splitFullName() that does not need to access anything (read or write data from the disks) and a variable scope that doesn't have to be part of the instance. It returns the firstname and lastname.

```luceescript
// example5.cfm
person = new Example5("Sobchak Walter");
dump(person.getLastName());
dump(Example5::splitFullName("Quintana Jesus"));
```

### Footnotes

[Lucee 5 features reviewed: static](https://dev.lucee.org/t/lucee-5-features-reviewed-static/433)

[Video: Lucee Static Scopes in Component Code](https://www.youtube.com/watch?v=B5ILIAbXBzo&feature=youtu.be)