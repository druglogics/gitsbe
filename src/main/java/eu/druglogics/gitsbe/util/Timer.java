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
		duration = endTime - startTime;
	}

	long getDuration() {
		return duration;
	}

	public int getMilliSecondsOfDuration() {
		return (int) (duration / 1000000);
	}

	public int getSecondsOfDuration() {
		return (int) (duration / 1000000000) % 60;
	}
	
	public int getMinutesOfDuration() {
		return (int) (((duration / 1000000000) / 60) % 60);
	}
	
	public int getHoursOfDuration() {
		return (int) (((duration / 1000000000) / (60 * 60)));
	}
	
}
