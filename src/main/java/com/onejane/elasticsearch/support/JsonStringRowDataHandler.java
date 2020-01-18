package com.onejane.elasticsearch.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.onejane.elasticsearch.entry.AbstractEntryHandler;

import java.util.List;

/**
 * @author: shimh
 * @create: 2019年11月
 **/
public class JsonStringRowDataHandler implements AbstractEntryHandler.RowDataHandler<String>{

    @Override
    public String handler(CanalEntry.EventType eventType, List<CanalEntry.Column> columnsList) {

        JSONObject json = new JSONObject();

        for (CanalEntry.Column column : columnsList) {
            json.put(column.getName(), column.getValue());
        }
        return JSON.toJSONString(json);

    }
}
