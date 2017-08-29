package net.yzimroni.bukkitanimations.animation;

import java.io.File;

public class Animation {

	private AnimationData data;
	private File file;

	public Animation(AnimationData data, File file) {
		super();
		this.data = data;
		this.file = file;
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

}
