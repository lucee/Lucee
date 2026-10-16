package lucee.runtime.config.maven.extensionlist;

import java.io.IOException;
import java.util.Set;

import lucee.runtime.config.maven.MavenUpdateProvider.Repository;

public interface ExtensionLister {

	String getName();

	Set<String> list(Repository repo, String groupId) throws IOException;

}
