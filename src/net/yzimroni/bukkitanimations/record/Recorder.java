package net.yzimroni.bukkitanimations.record;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.common.base.Preconditions;

import net.yzimroni.bukkitanimations.animation.AnimationData;
import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.utils.Utils;

public class Recorder {

	private AnimationData animation;
	private int tick = 1;

	private List<ActionData> actions = new ArrayList<ActionData>();

	// Extra files to add to the animation zip file (used to save schematics)
	private Map<String, Object> extraFiles = new HashMap<String, Object>();

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
	}

	public void addExtraFile(String path, Object data) {
		Preconditions.checkNotNull(path, "path");
		Preconditions.checkNotNull(data, "data");
		Preconditions.checkArgument(data instanceof byte[] || data instanceof File,
				"Extra file data is in unknown type: " + data.getClass());
		extraFiles.put(path, data);
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

			// Add extra files to the zip
			for (Entry<String, Object> extra : extraFiles.entrySet()) {
				byte[] data = null;
				if (extra.getValue() instanceof byte[]) {
					data = (byte[]) extra.getValue();
				} else if (extra.getValue() instanceof File) {
					data = Files.readAllBytes(((File) extra.getValue()).toPath());
				} else {
					zipFile.close();
					throw new IllegalArgumentException("Unknown extra file data type: "
							+ (extra.getValue() == null ? "null" : extra.getValue().getClass()));
				}

				ZipEntry extraEntry = new ZipEntry(extra.getKey());
				zipFile.putNextEntry(extraEntry);
				zipFile.write(data);
				zipFile.closeEntry();
			}

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
