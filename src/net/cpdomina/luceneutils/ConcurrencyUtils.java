package net.cpdomina.luceneutils;

/**
 * Concurrency-related utilities
 * 
 * @author Pedro Oliveira
 *
 */
public class ConcurrencyUtils {

	private ConcurrencyUtils(){};
	
	/**
	 * Starts the given {@link Runnable} tasks as daemons
	 * 
	 * @param tasks
	 */
	public static void startDaemon(Runnable... tasks) {		
		for(Runnable task: tasks) {
			Thread thread = new Thread(task);
			thread.setDaemon(true);
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();
		}
	}
	
	/**
	 * Starts the given {@link Runnable} tasks as normal threads
	 * @param tasks
	 */
	public static void start(Runnable... tasks) {
		for(Runnable task: tasks) {
			new Thread(task).start();
		}
	}
}
