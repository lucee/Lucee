package lucee.runtime.ai;

import lucee.commons.io.CharsetUtil;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.converter.JSONDateFormat;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;

public abstract class AIModelSupport implements AIModel {

	private String name;
	protected Struct raw;
	private String charset;

	public AIModelSupport(String name, Struct raw, String charset) {
		this.name = name;
		this.raw = raw;
		this.charset = charset;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return Caster.toString(raw.get(KeyConstants._description, null), null);
	}

	@Override
	public String toString() {
		try {
			JSONConverter json = new JSONConverter(false, CharsetUtil.toCharset(charset), JSONDateFormat.PATTERN_CF, false);
			return json.serialize(null, raw, SerializationSettings.SERIALIZE_AS_UNDEFINED, true);
		}
		catch (ConverterException e) {
			return raw.toString();
		}
	}

	@Override
	public Struct asStruct() {
		return raw;
	}
}
