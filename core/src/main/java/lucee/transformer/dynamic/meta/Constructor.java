package lucee.transformer.dynamic.meta;

import java.io.IOException;

public interface Constructor extends FunctionMember {
	public Object newInstance(Object... args) throws IOException;
}
