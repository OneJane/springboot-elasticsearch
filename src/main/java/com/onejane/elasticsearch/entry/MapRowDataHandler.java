package com.onejane.elasticsearch.entry;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class MapRowDataHandler implements AbstractEntryHandler.RowDataHandler<Map<String, String>> {


    @Override
    public Map<String, String> handler(CanalEntry.EventType eventType, List<CanalEntry.Column> columnsList) {

        Map<String, String> map = columnsList.stream().collect(Collectors.toMap(CanalEntry.Column::getName, CanalEntry.Column::getValue, (oldValue, newValue) -> newValue));

        return map;
    }
}
