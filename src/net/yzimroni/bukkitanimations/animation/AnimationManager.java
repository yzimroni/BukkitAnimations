package net.yzimroni.bukkitanimations.animation;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.google.common.base.Preconditions;
import com.google.gson.reflect.TypeToken;

import net.yzimroni.bukkitanimations.data.action.ActionData;
import net.yzimroni.bukkitanimations.utils.Utils;

public class AnimationManager {

	private static AnimationManager instance = new AnimationManager();
	public static final String FILE_EXTENSION = ".mcanimation";

	private File animationsFolder;

	private AnimationManager() {

	}

	public static AnimationManager get() {
		return instance;
	}

	public List<Animation> getAnimations() {
		return Arrays.stream(animationsFolder.listFiles(f -> f.getName().endsWith(FILE_EXTENSION))).map(this::readFile)
				.filter(Objects::nonNull).collect(Collectors.toList());
	}

	public List<AnimationData> getAnimationsData() {
		return Arrays.stream(animationsFolder.listFiles(f -> f.getName().endsWith(FILE_EXTENSION)))
				.map(this::readAnimationData).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public boolean hasAnimation(String name) {
		return new File(animationsFolder, name + FILE_EXTENSION).exists(); // TODO Validate the file
	}

	public AnimationData getAnimationData(String name) {
		File f = new File(animationsFolder, name + FILE_EXTENSION);
		if (f.exists()) {
			return readAnimationData(f);
		}
		return null;
	}

	public Animation getAnimation(String name) {
		File f = new File(animationsFolder, name + FILE_EXTENSION);
		if (f.exists()) {
			return readFile(f);
		}
		return null;
	}

	public ZipFile getAnimationZip(File f) {
		try {
			ZipFile zip = new ZipFile(f);
			Preconditions.checkArgument(zip.getEntry("info.json") != null,
					"Animation file doesn't contains info.json file!");
			Preconditions.checkArgument(zip.getEntry("actions.json") != null,
					"Animation file doesn't contains actions.json file!");
			return zip;
		} catch (ZipException e) {
			System.out.println("Invalid animation zip file: " + f.getAbsolutePath());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException was thrown while reading animation file: " + f.getAbsolutePath());
			e.printStackTrace();
		}
		return null;
	}

	private AnimationData readAnimationData(File f) {
		try {
			ZipFile zip = getAnimationZip(f);
			if (zip == null) {
				return null;
			}
			AnimationData data = Utils.GSON.fromJson(
					new InputStreamReader(zip.getInputStream(zip.getEntry("info.json"))), AnimationData.class);
			zip.close();
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Animation readFile(File f) {
		try {
			ZipFile zip = getAnimationZip(f);

			AnimationData data = Utils.GSON.fromJson(
					new InputStreamReader(zip.getInputStream(zip.getEntry("info.json"))), AnimationData.class);

			List<ActionData> actions = Utils.GSON.fromJson(
					new InputStreamReader(zip.getInputStream(zip.getEntry("actions.json"))),
					new TypeToken<List<ActionData>>() {
					}.getType());

			Animation animation = new Animation(data, f, actions);

			zip.close();
			return animation;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public File createAnimationFile(String name) {
		return new File(animationsFolder, name + FILE_EXTENSION);
	}

	public File getAnimationsFolder() {
		return animationsFolder;
	}

	public void setAnimationsFolder(File animationsFolder) {
		if (!animationsFolder.exists()) {
			animationsFolder.mkdirs();
		}
		this.animationsFolder = animationsFolder;
	}

}
