package com.onejane.elasticsearch.entry;

import com.alibaba.otter.canal.protocol.CanalEntry;

import java.util.List;

/**
 * canal更新mysql到es
 * com.onejane.elasticsearch.support.KafkaSearchEntryHandler#doInsert发送kafka消息
 * com.onejane.elasticsearch.controller.KafkaController#listenThing1接受kafka消息，并更新索引数据
 *
 * @param <T>
 */
public abstract class AbstractEntryHandler<T> {

    public interface RowDataHandler<T> {
        T handler(CanalEntry.EventType eventType, List<CanalEntry.Column> columnsList);
    }

    private RowDataHandler<T> rowDataHandler;

    public AbstractEntryHandler(RowDataHandler<T> rowDataHandler) {
        this.rowDataHandler = rowDataHandler;
    }


    public final void handler(CanalEntry.Entry entry) {

        if (entry.getEntryType().equals(CanalEntry.EntryType.ROWDATA)) {

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e);
            }

            CanalEntry.EventType eventType = rowChage.getEventType();

            switch (eventType) {
                case INSERT:
                    rowChage.getRowDatasList().forEach(rowData -> {
                        T t = rowDataHandler.handler(eventType, rowData.getAfterColumnsList());
                        doInsert(t);
                    });
                    break;
                case UPDATE:
                    rowChage.getRowDatasList().forEach(rowData -> {
                        T before = rowDataHandler.handler(eventType, rowData.getBeforeColumnsList());
                        T after = rowDataHandler.handler(eventType, rowData.getAfterColumnsList());
                        doUpdate(before, after);
                    });
                    break;
                case DELETE:
                    rowChage.getRowDatasList().forEach(rowData -> {
                        T t = rowDataHandler.handler(eventType, rowData.getBeforeColumnsList());
                        doDelete(t);
                    });
                    break;
                default:
                    break;
            }
        }

    }

    protected abstract void doInsert(T after);

    protected abstract void doUpdate(T before, T after);

    protected abstract void doDelete(T before);
}
