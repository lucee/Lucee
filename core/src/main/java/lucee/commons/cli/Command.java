/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package lucee.commons.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.op.Caster;
import lucee.runtime.process.UDFProcessListener;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.StructUtil;

public final class Command {

	public static Process createProcess(String cmdline, boolean translate) throws IOException {
		if (!translate)	return new ProcessBuilder(cmdline.split(" ")).start();
		return new ProcessBuilder(toArray(cmdline)).start();
	}

	public static Process createProcess(PageContext pc, String[] commands, String workingDir, Struct environment, boolean redirectErrorStream) throws IOException, ExpressionException {
		pc = ThreadLocalPageContext.get(pc);
		FileResource dir = null;
		if (!StringUtil.isEmpty(workingDir, true)) {
			Resource res = ResourceUtil.toResourceExisting(pc, workingDir);
			if (!res.isDirectory()) throw new IOException("CFEXECUTE Directory [" + workingDir + "] is not a existing directory");
			if (res instanceof FileResource) dir = (FileResource) res;
			else throw new IOException(
					"CFEXECUTE directory [" + workingDir + "] must be a local directory, scheme [" + res.getResourceProvider().getScheme() + "] is not supported in this context.");
		}
		ProcessBuilder processBuilder = new ProcessBuilder(commands);
		if (dir != null) processBuilder.directory(dir);
		if (redirectErrorStream) processBuilder.redirectErrorStream(true);

		if (environment != null) {
			Map<String, String> env = processBuilder.environment();
			Iterator<Entry<Key, Object>> it = environment.entryIterator();
			Entry<Key, Object> e;
			while (it.hasNext()) {
				e = it.next();
				env.put(e.getKey().toString(), e.getValue().toString());
			}
		}
		return processBuilder.start();
	}

	public static Process createProcess(PageContext pc, String[] commands) throws IOException, ExpressionException {
		return createProcess(pc, commands, null, null, false);
	}

	/**
	 * @param cmdline command line
	 * @param translate translate the command line or not
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static CommandResult execute(String cmdline, boolean translate) throws IOException, InterruptedException {
		if (!translate) return execute(Runtime.getRuntime().exec(cmdline));
		return execute(new ProcessBuilder(toArray(cmdline)).start());
	}

	public static CommandResult execute(String[] cmdline) throws IOException, InterruptedException {
		return execute(new ProcessBuilder(cmdline).start());
	}

	public static CommandResult execute(List<String> cmdline) throws IOException, InterruptedException {
		return execute(new ProcessBuilder(cmdline.toArray(new String[cmdline.size()])).start());
	}

	public static CommandResult execute(String cmd, String[] args) throws IOException, InterruptedException {
		return execute(StringUtil.merge(cmd, args));
	}

	public static CommandResult execute(Process p) throws IOException, InterruptedException {
		InputStream is = null;
		InputStream es = null;
		IOException ioe;
		try {
			StreamGobbler in = new StreamGobbler(is = p.getInputStream());
			StreamGobbler err = new StreamGobbler(es = p.getErrorStream());
			in.start();
			err.start();
			int exitCode = p.waitFor();
			if (exitCode != 0) {
				err.join();
				if ((ioe = err.getException()) != null) throw new IOException(ioe);
				String str = err.getString();
				if (!StringUtil.isEmpty(str)) throw new CommandException(str);
			}
			in.join();
			if ((ioe = in.getException()) != null) throw new IOException(ioe);

			return new CommandResult(in.getString(), err.getString(), exitCode);
		}
		finally {
			IOUtil.close(is, es);
		}
	}

	public static CommandResult execute(PageContext pc, Process p, long timeout, UDFProcessListener onProgress, UDFProcessListener onError ) throws IOException, InterruptedException {
		IOException ioe;
		ExecutorService executorService = ThreadUtil.createExecutorService(2);
		executorService.execute(() -> handleStream(pc, p, p.getInputStream(), onProgress) );
		if (onError!= null) {
			executorService.execute(() -> handleStream(pc, p, p.getErrorStream(), onError));
		}
		int exitCode = p.waitFor();

		executorService.shutdown();
		executorService.awaitTermination(timeout, TimeUnit.SECONDS);
		/*
		if (exitCode != 0) {
			err.join();
			if ((ioe = err.getException()) != null) throw new IOException(ioe);
			String str = err.getString();
			if (!StringUtil.isEmpty(str)) throw new CommandException(str);
		}
		*/

		return new CommandResult(null, null, exitCode);
	}

	public static List<String> toList(String str) {
		if (StringUtil.isEmpty(str)) return new ArrayList<String>();
		str = str.trim();
		StringBuilder sb = new StringBuilder();
		ArrayList<String> list = new ArrayList<String>();
		char[] carr = str.toCharArray();
		char c;// ,last=0;
		char inside = 0;
		for (int i = 0; i < carr.length; i++) {
			c = carr[i];
			// if(i>0)last=carr[i-1];
			switch (c) {
			// DELIMITER
			/*
			 * case '\\': if(i+1<carr.length){ sb.append(carr[++i]); } else sb.append(c); break;
			 */
			// QUOTE
			case '\'':
			case '"':
				if (inside == 0) {
					if (str.lastIndexOf(c) > i) inside = c;
					else sb.append(c);
				}
				else if (inside == c) {
					inside = 0;
				}
				else sb.append(c);
				break;
			// WHITE SPACE
			case ' ':
			case '\b':
			case '\t':
			case '\n':
			case '\r':
			case '\f':
				// if(last=='\\')sb.setCharAt(sb.length()-1,c);
				if (inside == 0) {
					populateList(sb, list);
				}
				else sb.append(c);
				break;
			// OTHERS
			default:
				sb.append(c);
			}
		}
		populateList(sb, list);

		return list;
	}

	public static String[] toArray(String str) {
		List<String> list = toList(str);
		return list.toArray(new String[list.size()]);
	}

	private static void populateList(StringBuilder sb, ArrayList<String> list) {

		String tmp = sb.toString();
		tmp = tmp.trim();
		if (!StringUtil.isEmpty(tmp)) list.add(tmp);
		sb.delete(0, sb.length());
	}

	private static void handleStream(PageContext pc, Process p, InputStream inputStream, UDFProcessListener listener) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				Object result = listener.listen(line);
				if (!Caster.toBooleanValue(result,true)){
					p.destroy();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class StreamGobbler extends Thread {

	InputStream is;
	private String str;
	private IOException ioe;

	StreamGobbler(InputStream is) {
		this.is = is;
	}

	@Override
	public void run() {
		try {
			str = IOUtil.toString(is, SystemUtil.getCharset());
		}
		catch (IOException ioe) {
			this.ioe = ioe;
		}
	}

	/**
	 * @return the str
	 */
	public String getString() {
		return str;
	}

	/**
	 * @return the ioe
	 */
	public IOException getException() {
		return ioe;
	}

}
