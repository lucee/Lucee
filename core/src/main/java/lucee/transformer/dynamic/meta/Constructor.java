package lucee.transformer.dynamic.meta;

public interface Constructor extends FunctionMember {
	public Object newInstance(Object... args) throws Exception;
}
