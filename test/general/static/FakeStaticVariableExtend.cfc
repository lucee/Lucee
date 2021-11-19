// LDEV-3465
component extends="FakeStaticVariableBase" {

    function test() {
        return variables.static.foo;
    }

}