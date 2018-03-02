component
	entityName	= 'CardProcessor'
	table		= 'card_processor'
	accessors	= 'true'
	persistent	= 'true'
{

	property name = 'id'					column = 'card_processor_id'				type	= 'string'		length = '32' fieldtype = 'id' generator = 'assigned';
	property name = 'name'					column = 'card_processor_name'				type	= 'string'		length = '32';


}
