![Lucee](https://raw.githubusercontent.com/lucee/Lucee/6.0/images/lucee-white.png#gh-dark-mode-only)
![Lucee](https://raw.githubusercontent.com/lucee/Lucee/6.0/images/lucee-black.png#gh-light-mode-only)


# Lucee 6

Lucee comes with a lot of new features and functionality that improve your interaction with Lucee both directly through new features and indirectly by enhancing the existing ones. The focus as always is not simply to add functionality that you can achieve yourself with CFML code but to enhance the language itself.

Stay tuned as we explore the exciting world of Lucee 6. Get ready to elevate your CFML game with the latest and greatest.

## Java

Lucee now offers an array of enhanced functionalities for a more seamless integration between Lucee and Java applications and code.

### Java Code inside CFML!

In Lucee 6 you have the flexibility to incorporate Java code directly within your CFML code opening up new possibilities for seamless integration.

#### Within a User Defined Function (UDF):

```java
int function echoInt(int i) type="java" {
    if(i==1) throw new Exception("Oopsie-daisy!!!");
    return i*2;
}
```

Simply add the attribute `[type="java"]` and you can effortlessly embed Java code within your CFML template.

#### Or in a Closure:

```java
to_string = function (String str1, String str2) type="java" {
    return new java.lang.StringBuilder(str1).append(str2).toString();
}
```

Please note that this feature isn't supported for Lambda Functions as the Lambda Syntax conflicts with the attribute "type=java".


### Components

In Lucee 6, we've expanded your options by introducing sub-components, a powerful addition to your CFML toolkit. Unlike traditional components defined in separate .cfc files, sub-components reside within the same template as the main component, offering you increased flexibility and improved code organization.

#### Why Use Sub Components?

Sub-components bring several benefits to the table:

- **Modularization**: Sub-components enable you to break down complex components into smaller, more manageable pieces. This modular approach simplifies development, testing, and maintenance.
- **Reusability**: Components that are used across multiple templates can be encapsulated as sub-components. This reusability reduces code duplication and promotes a DRY (Don't Repeat Yourself) coding practice.
- **Scoped Variables**: Sub-components can access variables within the parent component's scope, simplifying data sharing between related components.
- **Improved Collaboration**: In team projects, sub-components can make it easier for developers to work on specific parts of a component without affecting the entire structure. This promotes collaboration and concurrent development.
- **Code Organization**: Sub-components help maintain a clean and organized codebase by keeping related functionality together within a single template.
- **Testing Isolation**: When unit testing components, sub-components can be tested in isolation, allowing for more targeted and granular testing.
- **Easier Debugging**: Smaller, focused sub-components can make debugging more straightforward since you can pinpoint issues within specific sections of your code.
- **Version Control**: Sub-components can be managed and version-controlled independently, providing more control over changes and updates.
- **Performance Optimization**: In certain cases, splitting functionality into sub-components can lead to more efficient code execution, especially when components are conditionally loaded based on specific use cases.
- **Simplified Component Management**: With sub-components, you can manage related code within a single template, making it easier to locate and edit all associated functionality.

These additional benefits highlight the versatility and advantages of using sub-components in your CFML applications.

#### Example of Sub Component Definition:

```cfml
component {
    function mainTest() {
        return "main";
    }
}

component name="Sub" {  
    function subTest() {
        return "sub";
    }
}

// Usage example
cfc = new MyCFC();
echo("main -> " & cfc.mainTest());
echo("<br>");

cfc = new MyCFC$Sub();
echo("sub -> " & cfc.subTest());
echo("<br>");
```

Sub-components expand your horizons and provide new avenues for structuring your CFML code effectively.
