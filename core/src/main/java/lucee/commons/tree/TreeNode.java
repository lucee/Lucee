package lucee.commons.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TreeNode<T> {
	private T value;
	private List<TreeNode<T>> children;
	private Set<T> all;
	private TreeNode<T> parent;

	public TreeNode(T value) {
		this(value, false);
	}

	public TreeNode(T value, boolean allowIncest) {
		this.value = value;
		if (!allowIncest) all = new HashSet<T>();
	}

	private TreeNode(T value, TreeNode<T> parent) {
		this.value = value;
		all = parent.all;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public TreeNode<T> getParent() {
		return parent;
	}

	public List<TreeNode<T>> getChildren() {
		return children;
	}

	public boolean addChild(T child) {
		if (all != null) {
			if (all.contains(child)) return false;
			all.add(child);
		}
		if (children == null) children = new ArrayList<TreeNode<T>>();
		children.add(new TreeNode<T>(child, this));
		return true;
	}

	public List<T> asList() {
		List<T> list = new ArrayList<T>();
		list.add(getValue());
		asList(list, getChildren());
		return list;
	}

	private void asList(List<T> list, List<TreeNode<T>> children) {
		if (children == null) return;

		for (TreeNode<T> n: children) {
			list.add(n.getValue());
			asList(list, n.getChildren());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(value.toString()).append('\n');
		toString(sb, getChildren(), 1);
		return sb.toString();
	}

	private void toString(StringBuilder sb, List<TreeNode<T>> children, int level) {
		if (children == null) return;
		for (TreeNode<T> tn: children) {
			for (int i = 0; i < level; i++) {
				sb.append('-');
			}
			sb.append(' ').append(tn.getValue().toString()).append('\n');
			toString(sb, tn.getChildren(), level + 1);
		}
	}

}