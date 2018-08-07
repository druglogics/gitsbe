package eu.druglogics.gitsbe;

public class Timer {
	private long startTime;
	private long endTime;
	private long duration;
	
	public Timer() {
		startTimer();
	}
	
	public void startTimer() {
		startTime = System.nanoTime();
	}
	
	public void stopTimer() {
		endTime = System.nanoTime();
		calculateDuration();
	}
	
	public void calculateDuration() {
		duration = (endTime - startTime) / 1000000000;
	}
	
	public int getSecondsOfDuration() {
		return (int) (duration) % 60;
	}
	
	public int getMinutesOfDuration() {
		return (int) ((duration / 60) % 60);
	}
	
	public int getHoursOfDuration() {
		return (int) ((duration / (60 * 60)));
	}
	
}
