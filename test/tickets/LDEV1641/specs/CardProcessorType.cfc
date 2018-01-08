component
	entityName	= 'CardProcessorType'
	table		= 'card_processor_type'
	accessors	= 'true'
	persistent	= 'true'
{


	property name = 'id' 						column = 'card_processor_type_id'					type	= 'string'		length = '32' fieldtype = 'id' generator = 'assigned';
	property name = 'label'  					column = 'card_processor_type_name'					type	= 'string'		length = '128';

}
