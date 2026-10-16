package lucee.runtime.config.maven.extensionlist;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import lucee.runtime.config.maven.MavenUpdateProvider.Repository;

public final class EmptyExtensionLister implements ExtensionLister {

	@Override
	public String getName() {
		return "empty";
	}

	@Override
	public Set<String> list(Repository repo, String groupId) throws IOException {
		ExtensionListUtil.validateGroupId(groupId);
		ExtensionListUtil.logDebug(getName(), "skipping artifact listing for group [" + groupId + "] at [" + repo.getUrl() + "]");
		return Collections.emptySet();
	}

}
