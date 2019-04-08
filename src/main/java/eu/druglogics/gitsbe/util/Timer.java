package eu.druglogics.gitsbe.util;

public class Timer {
	private long startTime;
	private long endTime;
	private long duration;
	
	public Timer() {
		startTimer();
	}
	
	private void startTimer() {
		startTime = System.nanoTime();
	}
	
	public void stopTimer() {
		endTime = System.nanoTime();
		calculateDuration();
	}
	
	private void calculateDuration() {
		duration = (endTime - startTime) / 1000000000;
	}
	
	int getSecondsOfDuration() {
		return (int) (duration) % 60;
	}
	
	int getMinutesOfDuration() {
		return (int) ((duration / 60) % 60);
	}
	
	int getHoursOfDuration() {
		return (int) ((duration / (60 * 60)));
	}
	
}
