package net.cpdomina.luceneutils;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSet;

import com.google.common.collect.Maps;

/**
 * Lucene {@link Filter} that only accepts documents contained in a given {@link DocIdSet}.
 * 
 * @author Pedro Oliveira
 *
 */
public class SubReaderFilter extends Filter{

	private final DocIdSet docs;
	
	private Map<IndexReader, Integer> bases;

	public SubReaderFilter(IndexReader reader, DocIdSet docs) {
		this.docs = docs;
		
		IndexReader[] subreaders = reader.getSequentialSubReaders();
		if(subreaders != null && subreaders.length > 0) {
			bases = Maps.newHashMap();
			
			//Get the starting point of each subreader
			int docBase = 0;
			for(IndexReader sub: subreaders) {
				int readerSize = sub.maxDoc();	
				bases.put(sub, docBase);
				docBase += readerSize;
			}
		}
	}

	@Override
	public DocIdSet getDocIdSet(IndexReader subReader) throws IOException {

		if(bases == null || !bases.containsKey(subReader)) {
			return docs;
		}		

		int docBase = bases.get(subReader);
		
		int readerSize = subReader.maxDoc();		
		OpenBitSet filter = new OpenBitSet(readerSize);

		DocIdSetIterator iterator = docs.iterator();		
		int doc = iterator.advance(docBase);			

		while(doc < docBase + readerSize) {		
			filter.set(doc - docBase);
			doc = iterator.nextDoc();
		}
		return filter;
	}

}
