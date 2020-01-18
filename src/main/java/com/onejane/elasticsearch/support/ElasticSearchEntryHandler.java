package com.onejane.elasticsearch.support;

import com.onejane.elasticsearch.entry.AbstractEntryHandler;
import com.onejane.elasticsearch.entry.MapRowDataHandler;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;

import java.util.Map;

/**
 * @author: shimh
 * @create: 2019年10月
 **/
public class ElasticSearchEntryHandler extends AbstractEntryHandler<Map<String, String>> {

    private String index = "test";
    private String type = "test";
    private String idKey = "id";

    private BulkProcessor bulkProcessor;

    public ElasticSearchEntryHandler(BulkProcessor bulkProcessor) {
        super(new MapRowDataHandler());
        this.bulkProcessor = bulkProcessor;
    }

    @Override
    protected void doInsert(Map<String, String> after) {
        String id = after.get(idKey);
        if (id != null) {
            IndexRequest request = new IndexRequest(index, type, id);
            request.source(after);
            bulkProcessor.add(request);
        }
    }

    @Override
    protected void doUpdate(Map<String, String> before, Map<String, String> after) {
        doInsert(after);
    }

    @Override
    protected void doDelete(Map<String, String> before) {
        String id = before.get(idKey);
        if (id != null) {
            DeleteRequest request = new DeleteRequest(index, type, id);
            bulkProcessor.add(request);
        }
    }

}
