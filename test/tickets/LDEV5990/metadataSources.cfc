/**
 * Test fixture for LDEV-5990: inline attributes vs docblock annotations
 * @author Test Author
 * @version 1.0
 */
component displayname="MetadataTest" singleton=true {

	function inlineAttributeOnly() mixin="controller" {
		return "inline";
	}

	/**
	 * Function with docblock annotations only
	 * @changes-only.hint Only show differences
	 * @format.options json,text
	 */
	function docblockAnnotationsOnly() {
		return "docblock";
	}

	/**
	 * Function with both
	 * @someTag.hint Some tag hint
	 */
	function bothInlineAndDocblock() mixin="model" changesOnly=true {
		return "both";
	}

}
