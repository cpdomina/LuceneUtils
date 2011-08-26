package net.cpdomina.luceneutils;

import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.UnmodifiableIterator;

/**
 * {@link Iterator} over all the {@link Document Documents} from an {@link IndexReader} belonging to the given {@link DocIdSet}.
 * @author Pedro Oliveira
 *
 */
public class DocumentIterator extends UnmodifiableIterator<Document>{
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentIterator.class);

	private final IndexReader reader;
	private final DocIdSetIterator docs;
	private int current; 
	
	public DocumentIterator(IndexReader reader, DocIdSet docs) throws IOException {
		this.reader = reader;
		this.docs = docs.iterator();
		advance();
	}

	public boolean hasNext() {
		return current != -1 && current != DocIdSetIterator.NO_MORE_DOCS;
	}

	public Document next() {
		try {
			return reader.document(current);
		} catch (IOException e) {
			LOGGER.warn("Problem while reading document from IndexReader", e);
			return null;
		} finally {
			advance();
		}
	}
	
	private void advance() {
		try {
			current = docs.nextDoc();
		} catch (IOException e) {
			LOGGER.warn("Problem while getting next doc", e);
			current = DocIdSetIterator.NO_MORE_DOCS;
		}
	}

}
