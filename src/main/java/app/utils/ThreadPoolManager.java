package app.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Manages a thread pool for concurrent task execution. This class creates an ExecutorService with a
 * fixed number of threads and provides methods to submit tasks, wait for all tasks to complete, and
 * shut down the executor.
 */
public class ThreadPoolManager {

  private List<Future<?>> taskList = new ArrayList<>();
  private static ThreadPoolManager instance;
  private ExecutorService executor;
  private static final int TIME_OUT_IN_MINUTES = 60;
  private int threadCount = 0;

  /**
   * Private constructor for ThreadPoolManager. This constructor is private to prevent direct
   * instantiation and to implement the Singleton pattern where only one instance of
   * ThreadPoolManager can exist.
   */
  private ThreadPoolManager() {}

  /**
   * Sets the number of threads in the ExecutorService based on the {@param neededcores}. If
   * coreCount is not 1, then coreCount is divided by two to save resrouces, then threadCount is set
   * to minimum of neededCores and the number of available system processors.
   *
   * @param neededCores The desired number of threads to be used.
   */
  private void setThreadCount(int neededCores) {
    int coreCount = Runtime.getRuntime().availableProcessors();
    if (coreCount != 1) {
      coreCount /= 2;
    }
    threadCount = Math.min(neededCores, coreCount);
    executor = Executors.newFixedThreadPool(threadCount);
  }

  /**
   * Initializes or reinitializes the thread pool based on the specified number of threads. If the
   * thread pool is not already initialized or if the current number of threads differs from {@param
   * neededCores}, the thread pool is (re)initialized. If the thread pool is already initialized and
   * requires reinitialization, it is first shut down before being reinitialized with the new thread
   * count.
   *
   * @param neededCores The desired number of threads in the thread pool.
   * @param neededCores The number of threads required.
   */
  public void initializeFixedThreadPool(int neededCores) throws InterruptedException {
    if (!isInitialized() || threadCount != neededCores) {
      if (isInitialized()) {
        this.shutdownExecutor();
      }
      setThreadCount(neededCores);
    }
  }

  /**
   * Checks whether executor is initialized & not shut down
   *
   * @return true if executor is initialized and not shut down & false it is shut down or not
   *     initialized
   */
  public boolean isInitialized() {
    return executor != null && !executor.isShutdown();
  }

  /**
   * Gets the singleton instance of the ThreadPoolManager. If the instance does not exist, it is
   * created.
   *
   * @return The singleton instance of ThreadPoolManager.
   */
  public static synchronized ThreadPoolManager getInstance() {
    if (instance == null) {
      instance = new ThreadPoolManager();
    }
    return instance;
  }

  /**
   * Submits a Runnable task for execution in the thread pool. The task is added to the list of
   * Futures to keep track for completion.
   *
   * @param task The {@link Runnable} task to be executed.
   * @throws IllegalStateException if the ThreadPoolManager has been shut down, terminated or not
   *     initalized.
   */
  public void submitTask(Runnable task) {
    if (executor == null || executor.isShutdown() || executor.isTerminated()) {
      throw new IllegalStateException(
          "Error, ThreadPoolManager is either shutdown, terminated or executor is not initalized");
    }
    Future<?> future = executor.submit(task);
    taskList.add(future);
  }

  /**
   * Waits for all submitted tasks to complete execution. If any tasks encounter an exception, it is
   * logged to the error stream.
   */
  public void waitForAllTasksToComplete() {
    for (Future<?> task : taskList) {
      try {
        task.get();
      } catch (ExecutionException e) {
        System.err.println("A task encountered an exception: " + e.getCause());
        e.printStackTrace();
      } catch (InterruptedException ex) {
        System.err.println("A task was interrupted: " + ex.getCause());
        ex.printStackTrace();
      }
    }
    taskList.clear();
  }

  /**
   * Initiates an orderly shutdown of the ExecutorService. If the ExecutorService does not terminate
   * within the specified timeout, it attempts to stop all actively executing tasks.
   *
   * @throws InterruptedException if interrupted while waiting for the executor to terminate.
   */
  public void shutdownExecutor() throws InterruptedException {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(TIME_OUT_IN_MINUTES, TimeUnit.MINUTES)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
      System.err.println(
          "The conversion process was interrupted and pending tasks may have been cancelled, and some conversions may be incomplete.");
      throw e;
    }
  }

  /**
   * Executes a list of tasks.
   *
   * @param tasks The tasks to be executed.
   * @param taskExecutor The function that defines how each task is executed.
   * @param <T> The type of the tasks.
   */
  public <T> void executeConversionTasks(List<T> tasks, Consumer<T> taskExecutor) {
    for (T task : tasks) {
      this.submitTask(() -> taskExecutor.accept(task));
    }

    this.waitForAllTasksToComplete();
  }
}
