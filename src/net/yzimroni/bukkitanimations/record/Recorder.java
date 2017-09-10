package net.yzimroni.bukkitanimations.record;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.yzimroni.bukkitanimations.animation.AnimationData;
import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.utils.Utils;

public class Recorder {

	private AnimationData animation;
	private int tick = 1;

	private List<ActionData> actions = new ArrayList<ActionData>();

	public Recorder(AnimationData animation) {
		super();
		this.animation = animation;
	}

	protected void tick() {
		tick++;
	}

	public void addAction(ActionData action) {
		if (action.getTick() == -1) {
			action.setTick(tick);
		}
		actions.add(action);
		System.out.println(action);
	}

	public void writeAnimation(File file) {
		try {
			ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(file));

			ZipEntry info = new ZipEntry("info.json");
			zipFile.putNextEntry(info);
			zipFile.write(Utils.GSON.toJson(animation).getBytes());
			zipFile.closeEntry();

			ZipEntry actionsData = new ZipEntry("actions.json");
			zipFile.putNextEntry(actionsData);
			zipFile.write(Utils.GSON.toJson(actions).getBytes());
			zipFile.closeEntry();

			zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public AnimationData getAnimation() {
		return animation;
	}

	public void setAnimation(AnimationData animation) {
		this.animation = animation;
	}

	public int getTick() {
		return tick;
	}

	public void setTick(int tick) {
		this.tick = tick;
	}

	public List<ActionData> getActions() {
		return actions;
	}

	public void setActions(List<ActionData> actions) {
		this.actions = actions;
	}

}
