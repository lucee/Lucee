component extends="org.lucee.cfml.test.LuceeTestCase"  {

    variables.left = {
        "extensions": [
            {
                "id": "E99E43A5-C10E-41E9-878BFC82BAAD99CE",
                "name": "Quartz Scheduler Extension",
                "version": "1.0.0.47-RC",
                "resource": "${DISTROCORE_PATH}/extensions/quartz-extension-1.0.0.47-RC.lex"
            }
        ]
    };

    variables.right = {
        "extensions": [
            {
                "id": "0EDF8686-BDC8-442B-B4EFE093EA6CC0C4",
                "name": "Distrokid Tasks Extension",
                "version": "1.0.0.14",
                "resource": "${DISTROCORE_PATH}/extensions/dk.tasks.extension-1.0.0.14.lex"
            }
        ]
    };

    function run() {

        describe("ConfigMerge", function() {

            describe("extensions", function() {

                it("merges two configs with different extensions", function() {
                    local.left  = duplicate(variables.left);
                    local.right = duplicate(variables.right);

                    var result = ConfigMerge(local.left, local.right);

                    expect(result.extensions).toHaveLength(2);
                    expect(result.extensions[1].id).toBe("E99E43A5-C10E-41E9-878BFC82BAAD99CE");
                    expect(result.extensions[2].id).toBe("0EDF8686-BDC8-442B-B4EFE093EA6CC0C4");
                });

                it("does not modify the left input", function() {
                    local.left  = duplicate(variables.left);
                    local.right = duplicate(variables.right);

                    ConfigMerge(local.left, local.right);

                    expect(local.left.extensions).toHaveLength(1);
                    expect(local.left.extensions[1].id).toBe("E99E43A5-C10E-41E9-878BFC82BAAD99CE");
                });

                it("does not modify the right input", function() {
                    local.left  = duplicate(variables.left);
                    local.right = duplicate(variables.right);

                    ConfigMerge(local.left, local.right);

                    expect(local.right.extensions).toHaveLength(1);
                    expect(local.right.extensions[1].id).toBe("0EDF8686-BDC8-442B-B4EFE093EA6CC0C4");
                });
            });

        });

    }

}