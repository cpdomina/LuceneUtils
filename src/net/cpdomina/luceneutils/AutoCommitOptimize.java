package net.cpdomina.luceneutils;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Performs maintenance operations (commit & optimize) on a {@link IndexWriter} with a certain frequency or when the number of docs in memory achieves a certain number.
 * @author Pedro Oliveira
 *
 */
public class AutoCommitOptimize implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AutoCommitOptimize.class);
	
	private final IndexWriter writer;
	private final long commitFrequency;
	private final long optimizeFrequency;
	private final long refreshFrequency;
	private final int maxRamDocsCommit;
	private long lastCommit;
	private long lastOptimize;
	private boolean isActive;
	
	
	/**
	 * 
	 * @param writer The {@link IndexWriter} to maintain
	 * @param commitFrequency Maximum time, in ms, waiting for a commit
	 * @param optimizeFrequency Maximum time, in ms, waiting for an optimize
	 * @param refreshFrequency Refresh frequency, in ms
	 * @param maxRamDocsCommit Maximum number of docs in memory before a commit
	 */
	public AutoCommitOptimize(IndexWriter writer, long commitFrequency, long optimizeFrequency, long refreshFrequency, int maxRamDocsCommit) {
		this.writer = writer;
		this.commitFrequency = commitFrequency;
		this.optimizeFrequency = optimizeFrequency;
		this.refreshFrequency = refreshFrequency;
		this.maxRamDocsCommit = maxRamDocsCommit;		
		lastCommit = System.currentTimeMillis();
		lastOptimize = System.currentTimeMillis();
		isActive = true;
	}

	public void run() {
		while(isActive) {
			
			long startTime = System.currentTimeMillis();
			
			if(isActive) {
				commit(startTime);
			}
			
			if(isActive) {
				optimize(System.currentTimeMillis());
			}

			if(isActive) {
				try {
					Thread.sleep(refreshFrequency - (System.currentTimeMillis() - startTime));
				} catch (InterruptedException e) {
					break;
				}
			}	
		}
		
		LOGGER.info("Stopped");
	}
	
	private void commit(long time) {	
		if(time - lastCommit > commitFrequency || writer.numRamDocs() > maxRamDocsCommit) {
			try {
				writer.commit();
			} catch (IOException e) {
				LOGGER.warn("Problem while commiting index", e);
			}
			lastCommit = System.currentTimeMillis();
		}
	}
	
	private void optimize(long time) {	
		if(time - lastOptimize > optimizeFrequency) {
			try {
				writer.optimize();
			} catch (IOException e) {
				LOGGER.warn("Problem while optimizing index", e);
			}
			lastOptimize = System.currentTimeMillis();
		}
	}
	
	/**
	 * Stop this task, when possible
	 */
	public void stop() {
		isActive = false;
	}
}
