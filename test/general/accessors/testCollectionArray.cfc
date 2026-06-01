component accessors="true" {
	// fieldType="one-to-many" → addTags/hasTags/removeTags generated
	property name="tags" type="array" fieldType="one-to-many" cfc="Tag";
	// no fieldType → no collection helpers, only getLabels/setLabels
	property name="labels" type="array";
}
