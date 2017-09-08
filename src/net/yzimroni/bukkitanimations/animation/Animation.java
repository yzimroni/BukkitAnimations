package net.yzimroni.bukkitanimations.animation;

import java.io.File;
import java.util.List;

import net.yzimroni.bukkitanimations.data.action.ActionData;

public class Animation {

	private AnimationData data;
	private File file;
	private List<ActionData> actions;

	public Animation(AnimationData data, File file, List<ActionData> actions) {
		super();
		this.data = data;
		this.file = file;
		this.actions = actions;
	}

	public AnimationData getData() {
		return data;
	}

	public void setData(AnimationData data) {
		this.data = data;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public List<ActionData> getActions() {
		return actions;
	}

	public void setActions(List<ActionData> actions) {
		this.actions = actions;
	}

}
