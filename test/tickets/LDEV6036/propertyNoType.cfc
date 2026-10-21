component {
	// Script style - no type attribute
	property name="Mixins" inject="id:Plugins";
	// Script style - WITH explicit type
	property name="WithType" type="string" inject="id:Config";
	// Script style - WITH explicit type="any" (should be preserved!)
	property name="ExplicitAny" type="any" inject="id:Service";
}
