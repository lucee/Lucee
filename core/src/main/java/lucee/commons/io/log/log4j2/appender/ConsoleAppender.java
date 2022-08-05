package lucee.commons.io.log.log4j2.appender;

import java.io.Writer;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AbstractWriterAppender;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.WriterManager;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.CloseShieldWriter;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.engine.ThreadLocalPageContext;

/**
 * Appends log events to a {@link Writer}.
 */
@Plugin(name = "Writer", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class ConsoleAppender extends AbstractWriterAppender<WriterManager> {

	/**
	 * Builds WriterAppender instances.
	 */
	public static class Builder<B extends Builder<B>> extends AbstractAppender.Builder<B> implements org.apache.logging.log4j.core.util.Builder<ConsoleAppender> {

		private boolean follow = false;

		private Writer target;

		@Override
		public ConsoleAppender build() {
			final StringLayout layout = (StringLayout) getLayout();
			final StringLayout actualLayout = layout != null ? layout : PatternLayout.createDefaultLayout();
			return new ConsoleAppender(getName(), actualLayout, getFilter(), getManager(target, follow, actualLayout), isIgnoreExceptions(), getPropertyArray());
		}

		public B setFollow(final boolean shouldFollow) {
			this.follow = shouldFollow;
			return asBuilder();
		}

		public B setTarget(final Writer aTarget) {
			this.target = aTarget;
			return asBuilder();
		}
	}

	/**
	 * Holds data to pass to factory method.
	 */
	private static class FactoryData {
		private final StringLayout layout;
		private final String name;
		private final Writer writer;

		/**
		 * Builds instances.
		 *
		 * @param writer The OutputStream.
		 * @param type The name of the target.
		 * @param layout A String layout
		 */
		public FactoryData(final Writer writer, final String type, final StringLayout layout) {
			this.writer = writer;
			this.name = type;
			this.layout = layout;
		}
	}

	private static class WriterManagerFactory implements ManagerFactory<WriterManager, FactoryData> {

		/**
		 * Creates a WriterManager.
		 *
		 * @param name The name of the entity to manage.
		 * @param data The data required to create the entity.
		 * @return The WriterManager
		 */
		@Override
		public WriterManager createManager(final String name, final FactoryData data) {
			return new WriterManager(data.writer, data.name, data.layout, true);
		}
	}

	private static WriterManagerFactory factory = new WriterManagerFactory();

	/**
	 * Creates a WriterAppender.
	 *
	 * @param layout The layout to use or null to get the default layout.
	 * @param filter The Filter or null.
	 * @param target The target Writer
	 * @param follow If true will follow changes to the underlying output stream. Use false as the
	 *            default.
	 * @param name The name of the Appender (required).
	 * @param ignore If {@code "true"} (default) exceptions encountered when appending events are
	 *            logged; otherwise they are propagated to the caller. Use true as the default.
	 * @return The ConsoleAppender.
	 */
	@PluginFactory
	public static ConsoleAppender createAppender(StringLayout layout, final Filter filter, final Writer target, final String name, final boolean follow, final boolean ignore) {
		if (name == null) {
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_ERROR, "log-loading", "No name provided for WriterAppender");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new ConsoleAppender(name, layout, filter, getManager(target, follow, layout), ignore, null);
	}

	private static WriterManager getManager(final Writer target, final boolean follow, final StringLayout layout) {
		final Writer writer = new CloseShieldWriter(target);
		final String managerName = target.getClass().getName() + "@" + Integer.toHexString(target.hashCode()) + '.' + follow;
		return WriterManager.getManager(managerName, new FactoryData(writer, managerName, layout), factory);
	}

	@PluginBuilderFactory
	public static <B extends Builder<B>> B newBuilder() {
		return new Builder<B>().asBuilder();
	}

	private ConsoleAppender(final String name, final StringLayout layout, final Filter filter, final WriterManager manager, final boolean ignoreExceptions,
			final Property[] properties) {
		super(name, layout, filter, ignoreExceptions, true, properties, manager);
	}

}
