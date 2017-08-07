component
		persistent =        'true'
		table =             'testTable1214'
{
	/* ID & Template */
	property
			name =          'id'
			fieldtype =     'id';

	/* Other properties */
	property
			name =          'name'
			length =        500;
	property
			name =          'toggle'
			type =          'boolean'
			default =       1;

	public any function edit( required struct form ) {
		if( structKeyExists( arguments.form, 'fieldnames' ) ) {

			if( !structKeyExists( arguments.form, 'toggle' ) ) {
				this.setToggle( false );
			}

		}
	}

}