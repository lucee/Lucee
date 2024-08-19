package lucee.runtime.ai;

import lucee.runtime.type.Struct;

public interface AIModel {

	public String getName();

	public String getLabel();

	public String getDescription();

	public Struct asStruct();
}
