package lucee.runtime.db;

import java.io.Serializable;
import java.net.URLDecoder;

import org.w3c.dom.Element;

import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;

public class ParamSyntax implements Serializable {

	public static final ParamSyntax DEFAULT = new ParamSyntax("?", "&", "=");

	public final String leadingDelimiter;
	public final String delimiter;
	public final String separator;

	private ParamSyntax(String leadingDelimiter, String delimiter, String separator) {
		this.leadingDelimiter = leadingDelimiter;
		this.delimiter = delimiter;
		this.separator = separator;
	}

	public static ParamSyntax toParamSyntax(String leadingDelimiter, String delimiter, String separator) {
		if (DEFAULT.delimiter.equals(delimiter) && DEFAULT.leadingDelimiter.equals(leadingDelimiter) && DEFAULT.separator.equals(separator)) return DEFAULT;
		return new ParamSyntax(leadingDelimiter, delimiter, separator);
	}

	public static ParamSyntax toParamSyntax(Struct sct, ParamSyntax defaultValue) {
		Struct cps = Caster.toStruct(sct.get("customParameterSyntax", null), null);
		if (cps == null) cps = Caster.toStruct(sct.get("parameterSyntax", null), null);
		String del;
		String sep;
		String ledel;
		if (cps != null) {
			del = Caster.toString(cps.get("delimiter", null), null);
			if (StringUtil.isEmpty(del)) del = Caster.toString(cps.get("paramDelimiter", null), null);
			sep = Caster.toString(cps.get("separator", null), null);
			if (StringUtil.isEmpty(sep)) sep = Caster.toString(cps.get("paramSeparator", null), null);
			ledel = Caster.toString(cps.get("leadingDelimiter", null), null);
			if (StringUtil.isEmpty(ledel)) ledel = Caster.toString(cps.get("paramLeadingDelimiter", null), null);
		}
		else {
			del = Caster.toString(sct.get("delimiter", null), null);
			if (StringUtil.isEmpty(del)) del = Caster.toString(sct.get("paramDelimiter", null), null);
			if (StringUtil.isEmpty(del)) del = Caster.toString(sct.get("paramSyntaxDelimiter", null), null);
			sep = Caster.toString(sct.get("separator", null), null);
			if (StringUtil.isEmpty(sep)) sep = Caster.toString(sct.get("paramSeparator", null), null);
			if (StringUtil.isEmpty(sep)) sep = Caster.toString(sct.get("paramSyntaxSeparator", null), null);
			ledel = Caster.toString(sct.get("leadingDelimiter", null), null);
			if (StringUtil.isEmpty(ledel)) ledel = Caster.toString(sct.get("paramLeadingDelimiter", null), null);
			if (StringUtil.isEmpty(ledel)) ledel = Caster.toString(sct.get("paramSyntaxLeadingDelimiter", null), null);
		}
		if (StringUtil.isEmpty(del) || StringUtil.isEmpty(sep)) {
			return defaultValue;
		}

		if (StringUtil.isEmpty(ledel)) ledel = del;
		return toParamSyntax(ledel, del, sep);
	}

	public static ParamSyntax toParamSyntax(Element el, ParamSyntax defaultValue) {
		if (!el.hasAttribute("param-delimiter") || !el.hasAttribute("param-separator")) return defaultValue;
		String del = URLDecoder.decode(el.getAttribute("param-delimiter"));
		String ledel = el.getAttribute("param-leading-delimiter");
		String sep = el.getAttribute("param-separator");
		if (StringUtil.isEmpty(ledel)) ledel = del;
		return toParamSyntax(ledel, del, sep);
	}

	@Override
	public String toString() {
		return "delimiter:" + delimiter + ";leadingDelimiter:" + leadingDelimiter + ";separator:" + separator;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ParamSyntax)) return false;
		ParamSyntax other = (ParamSyntax) obj;
		return other.delimiter.equals(delimiter) && other.leadingDelimiter.equals(leadingDelimiter) && other.separator.equals(separator);
	}

}
