package net.mantagames.jgm.engine;

import paulscode.sound.SoundSystemConfig;

public class Sound {
	private String path;
	private SoundType soundType;
	private String sourcename;
	private float volume = 1;
	
	public Sound(String filePath) {
		this.path = filePath;
		this.soundType = SoundType.NORMAL;
	}
	
	public Sound setSoundType(SoundType soundType) {
		this.soundType = soundType;
		stop();
		return this;
	}
	
	public void setVolume(float val) {
		this.volume = val;
	}
	
	protected void play(boolean loop) {
		if (soundType.equals(SoundType.BACKGROUND)) {
			stop();
		}
		this.sourcename = Runner.soundSystem.quickPlay(true, path, loop, 0, 0, 0, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff() );
		Runner.soundSystem.setVolume(sourcename, volume);
	}
	
	protected void stop() {
		if (sourcename != null) {
			//if (Runner.soundSystem.playing(sourcename)) {
				Runner.soundSystem.setLooping(sourcename, false);
				Runner.soundSystem.stop(this.sourcename);
				Runner.soundSystem.flush(sourcename);
				Runner.soundSystem.removeSource(sourcename);
			//}
		}
	}
	
	public enum SoundType {
		NORMAL, BACKGROUND;
	}
}
