package org.molgenis.data.semanticsearch.explain.service;

import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Explanation;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.semanticsearch.explain.bean.ExplainedQueryString;

public interface ElasticSearchExplainService
{
	/**
	 * Get explanation for a specific document in elasticSearch
	 * 
	 * @param documentId
	 * @param queryString
	 * @return
	 */
	abstract Explanation explain(Query<Entity> q, EntityMetaData entityMetaData, String documentId);

	/**
	 * Deduce all the matches that are generated by ElasticSearch
	 * 
	 * @param explanationBean
	 * @return
	 */
	abstract Set<ExplainedQueryString> findQueriesFromExplanation(Map<String, String> collectExpanedQueryMap,
			Explanation explanation);
}
