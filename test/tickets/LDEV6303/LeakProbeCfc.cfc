// Dedicated CFC class for the leak-catcher test. Must NOT be instantiated
// outside the leak test, so the test's instance is genuinely the first
// construction of this class — that's the moment when (on 7.0) the first
// instance's variables-scope value gets aliased with the class-level _default.
component accessors=true {
	property name="bag" type="any" default='#{"n":1}#';
}
