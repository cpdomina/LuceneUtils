package net.cpdomina.luceneutils;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.OpenBitSet;

/**
 * Lucene {@link Collector} that gathers the DocIds in a {@link OpenBitSet}
 * 
 * @author Pedro Oliveira
 *
 */
public class DocIDCollector extends Collector {

	private final OpenBitSet docs;
	private int docBase;
	
	/**
	 * @param size Initial OpenBitSet size
	 */
	public DocIDCollector(int size) {
		docs = new OpenBitSet(size);
	}
	
	public DocIDCollector() {
		docs = new OpenBitSet();
	}
	
	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void collect(int doc) throws IOException {
		docs.set(doc + docBase);
	}

	@Override
	public void setNextReader(IndexReader subreader, int docBase) throws IOException {
		this.docBase = docBase;
	}

	@Override
	public void setScorer(Scorer scorer) throws IOException {
		
	}

	public OpenBitSet getDocs() {
		return docs;
	}

}
