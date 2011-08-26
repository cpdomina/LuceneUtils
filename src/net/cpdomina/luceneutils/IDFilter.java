package net.cpdomina.luceneutils;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

/**
 * Lucene {@link Filter} that only accepts documents with one of the given values in a certain field.
 * 
 * @author Pedro Oliveira
 *
 */
public class IDFilter extends Filter {
	
	private final Set<String> ids;
	private final String field;

	public IDFilter(Set<String> ids, String field) {
		this.ids = ids;
		this.field = field;
	}

	@Override
	public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
		OpenBitSet bits = new OpenBitSet(reader.maxDoc());
		
		int[] docs = new int[1];
		int[] freqs = new int[1];
		for(String id: ids) {
			TermDocs termDocs = reader.termDocs(new Term(field, id));
			int count = termDocs.read(docs, freqs);
			if(count == 1) {
				bits.set(docs[0]);
			}
		}
		//TODO we can wrap with CachingWrapperFilter to cache docIDSets.
		return bits;
	}

}
