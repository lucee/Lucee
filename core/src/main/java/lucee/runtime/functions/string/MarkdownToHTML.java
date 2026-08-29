package lucee.runtime.functions.string;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.image.attributes.ImageAttributesExtension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;

import lucee.runtime.PageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

import java.util.List;

public class MarkdownToHTML extends BIF implements Function {

	private static final long serialVersionUID = 3775127934350736736L;

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length < 1 || args.length > 2) {
			throw new FunctionException(pc, "MarkdownToHTML", 1, 2, args.length);
		}
		return call(pc, Caster.toString(args[0]));
	}

	public static String call(PageContext pc, String markdown) {
		return call(pc, markdown, false, null);
	}

	public static String call(PageContext pc, String markdown, boolean safeMode) {
		return call(pc, markdown, safeMode, null);
	}

	public static String call(PageContext pc, String markdown, boolean safeMode, String encoding) {
		List<Extension> extensions = List.of(
			TablesExtension.create(),
			AutolinkExtension.create(),
			ImageAttributesExtension.create(),
			HeadingAnchorExtension.create()
		);
		Parser parser = Parser.builder().extensions(extensions).build();
		// Parse the markdown to a Node
		Node document = parser.parse(markdown);
		// Create a HTML renderer
		HtmlRenderer renderer = HtmlRenderer.builder().extensions(extensions).build();
		// Render the Node to HTML
		return renderer.render(document);
	}

	/*
	 * public static void main(String[] args) { print.e(Processor.process("This is ***TXTMARK***",
	 * false)); }
	 */
}