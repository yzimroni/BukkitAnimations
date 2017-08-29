package net.yzimroni.bukkitanimations.animation;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.yzimroni.bukkitanimations.BukkitAnimationsPlugin;

public class AnimationManager {

	private static AnimationManager instance = new AnimationManager();
	public static final String FILE_EXTENSION = ".mcanimation";

	private File animationsFolder;

	public AnimationManager() {
		animationsFolder = new File(BukkitAnimationsPlugin.get().getDataFolder(), "animations");
		animationsFolder.mkdirs();
	}

	public static AnimationManager get() {
		return instance;
	}

	public List<Animation> getAnimations() {
		return Arrays.stream(animationsFolder.listFiles(f -> f.getName().endsWith(FILE_EXTENSION))).map(this::readFile)
				.collect(Collectors.toList());
	}

	public boolean hasAnimation(String name) {
		return new File(animationsFolder, name + FILE_EXTENSION).exists(); // TODO Validate the file
	}

	public Animation getAnimation(String name) {
		File f = new File(animationsFolder, name + FILE_EXTENSION);
		if (f.exists()) {
			return readFile(f);
		}
		return null;
	}

	private Animation readFile(File f) {
		AnimationData data = new AnimationData(f.getName().substring(0, f.getName().lastIndexOf(".")), null); // TODO
		Animation animation = new Animation(data, f);
		return animation;
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
