package net.cpdomina.luceneutils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.io.Closeables;

/**
 * Checks and maintains the uniqueness of a certain field's values in an Index. Useful when adding a new document triggers other expensive actions, like updating other documents.
 * Uses an internal real-time {@link IndexReader} and a {@link Set} of IDs to check the uniqueness of values.
 * 
 * @author Pedro Oliveira
 *
 */
public class UniqueIDWriter implements Closeable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(UniqueIDWriter.class);

	private int cacheSize;
	private Set<String> cache;
	private IndexReader reader;

	private final String field;
	private final IndexWriter writer;
	private final Freshness fresh;

	/**
	 * @param writer The {@link IndexWriter} to update
	 * @param field The field that will be unique
	 * @param cacheSize The number of ids in memory before updating the internal {@link IndexReader}
	 * @param updateTime The maximum number of ms the internal {@link IndexReader} can be without updating
	 * @throws IOException
	 */
	public UniqueIDWriter(IndexWriter writer, String field, int cacheSize, long updateTime) throws IOException {
		this.field = field;
		this.cacheSize = cacheSize;
		this.writer = writer;
		cache = Sets.newHashSet();
		reader = IndexReader.open(writer, true);

		fresh = new Freshness(updateTime);
		ConcurrencyUtils.startDaemon(fresh);
	}

	/**
	 * Check if the if there's already a {@link Document} with the given value on this {@link UniqueIDWriter} field
	 * @param value
	 * @return
	 */
	public synchronized boolean contains(String value) {
		if(cache.contains(value)) {
			return true;
		}

		try {
			if(reader.docFreq(new Term(field, value)) > 0) {
				return true;
			}
		} catch (IOException e) {
			LOGGER.warn("Problem while reading from IndexReader", e);
		}

		return false;
	}

	/**
	 * Insert the given {@link Document}, maintaining the uniqueness of the field
	 * 
	 * @param doc
	 * @throws IOException
	 */
	public synchronized void insert(Document doc) throws IOException {
		String value = doc.get(field);
		if(value != null) {
			writer.updateDocument(new Term(field, value), doc);

			try {
				if(cache.size() >= cacheSize) {
					update();
				}
			} finally {
				cache.add(value);
			}
		}
	}

	private synchronized void update() throws IOException {
		updateReader();
		cache.clear();
	}

	private void updateReader() throws IOException {
		IndexReader reopen = reader.reopen(true);
		if(reopen != reader) {
			Closeables.closeQuietly(reader);
		}
		reader = reopen;
	}

	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	public void close() {
		Closeables.closeQuietly(reader);
		fresh.isActive = false;
	}

	/**
	 * Maintains the freshness of the {@link IndexReader}
	 * 
	 * @author Pedro Oliveira
	 *
	 */
	private class Freshness implements Runnable {
		boolean isActive;
		private final long sleep;

		public Freshness(long sleep) {
			isActive = true;
			this.sleep = sleep;
		}

		public void run() {
			do {

				try {
					Thread.sleep(sleep);
				} catch (InterruptedException e) {
					isActive = false;
				}
				if(isActive) {
					try {
						update();
					} catch (IOException e) {
						LOGGER.warn("Problem while updating IndexReader", e);
					}
				}

			} while(isActive);
		}	
	}

}
