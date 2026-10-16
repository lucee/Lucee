component accessors="true" {
	// fieldType="one-to-many" → addMeta/hasMeta/removeMeta generated
	property name="meta" type="struct" fieldType="one-to-many" cfc="Meta";
	// no fieldType → no collection helpers, only getConfig/setConfig
	property name="config" type="struct";
}
