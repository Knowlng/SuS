package app.utils;

/** A simple timing utility class for measuring durations of tasks and laps. */
public class Timer {

  private long startTime;
  private long lastLapTime;

  /**
   * Starts the timer. Sets the current system time in milliseconds to both the start time and the
   * last lap time.
   */
  public void start() {
    startTime = System.currentTimeMillis();
    lastLapTime = startTime;
  }

  /**
   * Records a lap, calculates the duration since the last lap time in milliseconds and updates the
   * last lap time.
   *
   * @return the duration of the lap in milliseconds.
   */
  public long lap() {
    long now = System.currentTimeMillis();
    long lapTime = now - lastLapTime;
    lastLapTime = now;
    return lapTime;
  }

  /**
   * Stops the timer and calculates the total duration since the timer was started in milliseconds.
   *
   * @return the total time elapsed since the timer was started in milliseconds.
   */
  public long stop() {
    long endTime = System.currentTimeMillis();
    return endTime - startTime;
  }

  /**
   * Formats a duration in milliseconds into a human-readable string.
   *
   * @param durationMillis the duration in milliseconds to format.
   * @return a formatted string representing the duration in minutes and seconds if minutes are
   *     present, or just seconds otherwise.
   */
  public static String formatDuration(long durationMillis) {
    long seconds = (durationMillis / 1000) % 60;
    long minutes = (durationMillis / (1000 * 60)) % 60;

    if (minutes > 0) {
      return String.format("%d min %d s", minutes, seconds);
    } else {
      return String.format("%d s", seconds);
    }
  }

  /**
   * Prints the duration of a task in a human-readable format.
   *
   * @param taskName the name of the task that was timed.
   * @param durationMillis the duration in milliseconds that the task took.
   */
  public static void printDuration(String taskName, long durationMillis) {
    System.out.println(taskName + " completed in: " + formatDuration(durationMillis));
  }
}
