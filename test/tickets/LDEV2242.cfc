component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults , testBox ) {

		// Debug output: print sorted property metadata
		var comp = getComp();
		var propsByName = getPropsByName(comp);
		var funcsByName = getFuncsByName(comp);
		/*
		systemOutput("[DEBUG] comp: " & serializeJSON(comp), true);
		systemOutput("[DEBUG] getPropsByName(comp): " & serializeJSON(propsByName), true);
		systemOutput("[DEBUG] getFuncsByName(comp): " & serializeJSON(funcsByName), true);
		*/

		describe("LDEV-2242 property type metadata", function() {
			var props = getPropsByName(getComp());
			it("array property type", function() {
				expect(props["arrayProp"].type).toBe("array");
			});
			it("string property type", function() {
				expect(props["stringProp"].type).toBe("string");
			});
			it("struct property type", function() {
				expect(props["structProp"].type).toBe("struct");
			});
			it("testcfc property type", function() {
				expect(props["testcfcProp"].type).toBe("testcfc");
			});
			it("logbox property type", function() {
				expect(props["logbox"].type).toBe("logbox");
			});
			it("logboxNoType property type (any)", function() {
				expect(props["logboxNoType"].type).toBe("any");
			});
		});

		describe("LDEV-2242 function returnType metadata", function() {
			var funcs = getFuncsByName(getComp());
			it("array getter returnType", function() {
				expect(funcs["getArrayProp"].returntype).toBe("array");
			});
			it("string getter returnType", function() {
				expect(funcs["getStringProp"].returntype).toBe("string");
			});
			it("struct getter returnType", function() {
				expect(funcs["getStructProp"].returntype).toBe("struct");
			});
			it("testcfc getter returnType", function() {
				// Lucee 6.2: FAIL - Expected [testcfc] but received [any]
				expect(funcs["getTestcfcProp"].returntype).toBe("testcfc");
			});
			it("logbox getter returnType", function() {
				// Lucee 6.2: FAIL - Expected [logbox] but received [any]
				expect(funcs["getLogbox"].returntype).toBe("logbox");
			});
			it("logboxNoType getter returnType (any)", function() {
				expect(funcs["getLogboxNoType"].returntype).toBe("any");
			});
		});

		describe("LDEV-2242 implicit getter returnType", function() {
			it("getStringProp implicit getter returnType", function() {
				expect(getMetadata(getComp()["getStringProp"]).returntype).toBe("string");
			});
			it("getArrayProp implicit getter returnType", function() {
				expect(getMetadata(getComp()["getArrayProp"]).returntype).toBe("array");
			});
			it("getStructProp implicit getter returnType", function() {
				expect(getMetadata(getComp()["getStructProp"]).returntype).toBe("struct");
			});
			it("gettestcfcProp implicit getter returnType", function() {
				// Lucee 6.2: FAIL - Expected [testcfc] but received [any]
				expect(getMetadata(getComp()["gettestcfcProp"]).returntype).toBe("testcfc");
			});
			it("getLogbox implicit getter returnType", function() {
				// Lucee 6.2: FAIL - Expected [logbox] but received [any]
				expect(getMetadata(getComp()["getLogbox"]).returntype).toBe("logbox");
			});
			it("getLogboxNoType implicit getter returnType (any)", function() {
				expect(getMetadata(getComp()["getLogboxNoType"]).returntype).toBe("any");
			});
		});
	}

	private function getComp() {
		return new LDEV2242.testComponent();
	}

	private function getPropsByName(comp) {
		var meta = getMetadata(comp);
		var props = {};
		for (var p in meta.properties) {
			props[p.name] = p;
		}
		return props;
	}

	private function getFuncsByName(comp) {
		var meta = getMetadata(comp);
		var funcs = {};
		for (var f in meta.functions) {
			funcs[f.name] = f;
		}
		return funcs;
	}

}