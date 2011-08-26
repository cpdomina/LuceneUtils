LuceneUtils
===========

[Apache Lucene](http://lucene.apache.org/java/docs/index.html) utilities.

Auto Commit/Optimize
--------------------

Performs maintenance operations (commit & optimize) on a IndexWriter with a certain frequency or when the number of docs in memory achieves a certain number.

	//Commit every 30 minutes or when there are 10k docs in memory
	//Optimize every day
	//Wakeup thread for changes every 5 minutes
	AutoCommitOptimize optimizer = new AutoCommitOptimize(
			writer, 
			TimeUnit.MINUTES.toMillis(30), 
			TimeUnit.DAYS.toMillis(1), 
			TimeUnit.MINUTES.toMillis(5), 
			10000
	);
	
	//Start the optimizer in a Thread
	new Thread(optimizer).start();
	
	//Stop the optimizer and the thread
	optimizer.stop();


DocID Collector
---------------

Lucene Collector that simply gathers all the DocIds in a OpenBitSet.

	//Create collector
	DocIDCollector collector = new DocIDCollector();
	
	//Search
	searcher.search(query, collector);
	
	//Get the DocIDs returned by the search
	OpenBitSet docs = collector.getDocs();


Document Iterator
-----------------

Iterator over all the Documents in a DocIdSet.

	//Build iterator
	DocumentIterator iterator = new DocumentIterator(reader, docIdSet);
	
	//Iterate over Documents
	while(iterator.hasNext()) {
		Document doc = iterator.next();
	}
	

ID Filter
---------

Lucene Filter that only accepts documents with one of the given values in a certain field.

	//Create filter
	IDFilter filter = new IDFilter(values, "field");
	
	//Search
	searcher.search(query, filter, collector);


SubReader Filter
----------------

Lucene Filter that only accepts documents contained in a given DocIdSet.

	//Create filter
	SubReaderFilter filter = new SubReaderFilter(reader, docIdSet);
	
	//Search
	searcher.search(query, filter, collector);
	

Unique ID Writer
----------------

Checks and maintains the uniqueness of a certain field's values in an Index. Useful when adding a new document triggers other expensive actions, like updating other documents.
Uses an internal real-time IndexReader and a cache of IDs to check the uniqueness of values.
	
	//Maintain uniqueness of field "id"
	//Update internal IndexReader when cache = 1k or every 30 minutes
	UniqueIDWriter unique = new UniqueIDWriter(
			writer, 
			"id", 
			1000, 
			TimeUnit.MINUTES.toMillis(30)
	);
	
	//Check if index contains document with the same id. If not, insert it
	if(!unique.contains(doc.get("id"))) {
		unique.insert(doc);
		updateOtherDocuments();
	}
	
	//Release internal resources
	unique.close();
