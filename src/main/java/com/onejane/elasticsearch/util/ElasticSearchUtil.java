package com.onejane.elasticsearch.util;

import com.alibaba.fastjson.JSON;
import com.onejane.elasticsearch.bean.EsEntity;
import com.onejane.elasticsearch.bean.EsPage;
import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ScrolledPage;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @program: springboot-elasticsearch
 * @description: es常用工具类
 * @author: OneJane
 * @create: 2020-01-12 14:23
 **/
@Component
public class ElasticSearchUtil<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchUtil.class);
    @Autowired
    private RestHighLevelClient rhlClient;
    private static RestHighLevelClient client;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    /**
     * scroll游标快照超时时间，单位ms
     */
    private static final long SCROLL_TIMEOUT = 3000;

    @PostConstruct
    public void init() {
        client = this.rhlClient;
    }

    /**
     * 判断索引是否存在     *
     *
     * @param index 索引，类似数据库
     * @return boolean
     * @auther: LHL
     */
    public static boolean isIndexExist(String index) {
        boolean exists = false;
        try {
            exists = client.indices().exists(new GetIndexRequest().indices(index), RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (exists) {
            LOGGER.info("Index [" + index + "] is exist!");
        } else {
            LOGGER.info("Index [" + index + "] is not exist!");
        }
        return exists;
    }

    /**
     * 创建索引以及映射mapping，并给索引某些字段指定iK分词，以后向该索引中查询时，就会用ik分词。
     *
     * @param: indexName  索引，类似数据库
     * @return: boolean
     * @auther: LHL
     */
    public static boolean createIndex(String indexName) {
        if (!isIndexExist(indexName)) {
            LOGGER.info("Index is not exits!");
        }
        CreateIndexResponse createIndexResponse = null;
        try {
            //创建映射
            XContentBuilder mapping = null;
            try {
                mapping = XContentFactory.jsonBuilder()
                        .startObject()
                        .startObject("properties")
                        //.startObject("m_id").field("type","keyword").endObject()  //m_id:字段名,type:文本类型,analyzer 分词器类型
                        //该字段添加的内容，查询时将会使用ik_max_word 分词 //ik_smart  ik_max_word  standard
                        .startObject("id")
                        .field("type", "text")
                        .endObject()
                        .startObject("title")
                        .field("type", "text")
                        .field("analyzer", "ik_max_word")
                        .endObject()
                        .startObject("content")
                        .field("type", "text")
                        .field("analyzer", "ik_max_word")
                        .endObject()
                        .startObject("state")
                        .field("type", "text")
                        .endObject()
                        .endObject()
                        .startObject("settings")
                        //分片数
                        .field("number_of_shards", 5)
                        //副本数
                        .field("number_of_replicas", 1)
                        .endObject()
                        .endObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
            CreateIndexRequest request = new CreateIndexRequest(indexName).source(mapping);
            //设置创建索引超时2分钟
            request.timeout(TimeValue.timeValueMinutes(2));
            createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return createIndexResponse.isAcknowledged();
    }


    /**
     * 数据添加,一条文档
     *
     * @param content   要增加的数据
     * @param indexName 索引，类似数据库
     * @param id        id
     * @return String
     * @auther: LHL
     */
    public static String addData(XContentBuilder content, String indexName, String typeName, String id) {
        IndexResponse response = null;
        try {
            IndexRequest request = new IndexRequest(indexName).type(typeName).id(id).source(content);
            response = client.index(request, RequestOptions.DEFAULT);
            LOGGER.info("addData response status:{},id:{}", response.status().getStatus(), response.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response.getId();
    }


    /**
     * 批量添加数据
     *
     * @param list  要批量增加的数据
     * @param index 索引，类似数据库
     * @return
     * @auther: LHL
     */
    public void insertBatch(String index, String typeName, List<EsEntity> list) {
        BulkRequest request = new BulkRequest();
        list.forEach(item -> request.add(new IndexRequest(index).type(typeName).id(item.getId())
                .source(JSON.toJSONString(item.getData()), XContentType.JSON)));
        try {
            client.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 根据条件删除
     *
     * @param builder   要删除的数据  new TermQueryBuilder("userId", userId)
     * @param indexName 索引，类似数据库
     * @return
     * @auther: LHL
     */
    public void deleteByQuery(String indexName, String typeName, SearchSourceBuilder builder) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName).types(typeName);
        searchRequest.source(builder);
        //设置批量操作数量,最大为10000
        searchRequest.setBatchedReduceSize(100000);
        SearchResponse response = rhlClient.search(searchRequest, RequestOptions.DEFAULT);
        BulkRequest bulkRequest = new BulkRequest();

        if (response != null && response.getHits() != null) {
            SearchHits hits = response.getHits();
            List<String> docIds = Arrays.stream(hits.getHits()).map(e -> e.getId()).collect(Collectors.toList());
            for (String id : docIds) {
                DeleteRequest deleteRequest = new DeleteRequest(indexName, typeName, id);
                bulkRequest.add(deleteRequest);
            }
        }

        try {
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 批量删除
     *
     * @param idList 要删除的数据id
     * @param index  索引，类似数据库
     * @return
     * @auther: LHL
     */
    public static <T> void deleteBatch(String index, String type, Collection<T> idList) {
        BulkRequest request = new BulkRequest();
        idList.forEach(item -> request.add(new DeleteRequest(index, type, item.toString())));
        try {
            client.bulk(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 用于将Scroll获取到的结果，处理成dto列表，做复杂映射
     */
    private final SearchResultMapper searchResultMapper = new SearchResultMapper() {
        @Override
        public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
            List<T> result = new ArrayList<T>();
            for (SearchHit hit : response.getHits()) {
                if (response.getHits().getHits().length <= 0) {
                    return new AggregatedPageImpl<T>(Collections.EMPTY_LIST, pageable, response.getHits().getTotalHits(), response.getScrollId());
                }
                result.add(JSON.parseObject(JSON.toJSONString(hit.getSourceAsMap()), aClass));
            }
            if (result.isEmpty()) {
                return new AggregatedPageImpl<T>(Collections.EMPTY_LIST, pageable, response.getHits().getTotalHits(), response.getScrollId());
            }
            return new AggregatedPageImpl<T>((List<T>) result, pageable, response.getHits().getTotalHits(), response.getScrollId());
        }
    };


    /**
     * 使用分词查询  排序 ,并分页
     *
     * @param index     索引名称
     * @param startPage 当前页
     * @param pageSize  每页显示条数
     * @param query     查询条件
     * @return 结果
     */
    public EsPage<T> searchDataPage(String index, String type, int startPage, int pageSize, String sortField, QueryBuilder query, Class<T> tClass) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(query)
                .withIndices(index)
                .withSort(SortBuilders.fieldSort(sortField).order(SortOrder.ASC))
                .withTypes(type)
                .withPageable(PageRequest.of(startPage, pageSize))//从startPage页开始查，每页pageSize个结果
                .build();
        if (startPage <= 0) {
            startPage = 0;
        }
        //如果 pageSize是10 那么startPage>9990 (10000-pagesize) 如果 20  那么 >9980 如果 50 那么>9950
        //深度分页
        if (startPage > (10000 - pageSize)) {

            List<T> entityList = new ArrayList<T>();
            ScrolledPage<T> scroll = (ScrolledPage<T>) elasticsearchTemplate.startScroll(SCROLL_TIMEOUT, searchQuery, tClass, searchResultMapper);
            while (scroll.hasContent()) {
                entityList = (List<T>) scroll.getContent();
                //取下一页，scrollId在es服务器上可能会发生变化，需要用最新的。发起continueScroll请求会重新刷新快照保留时间
                scroll = (ScrolledPage<T>) elasticsearchTemplate.continueScroll(scroll.getScrollId(), SCROLL_TIMEOUT, tClass, searchResultMapper);
            }
            //及时释放es服务器资源
            elasticsearchTemplate.clearScroll(scroll.getScrollId());
            return new EsPage<T>(startPage, pageSize, Math.toIntExact(scroll.getTotalElements()), entityList);

        } else {
            // 浅层分页
            AggregatedPage<T> queryForPage = elasticsearchTemplate.queryForPage(searchQuery, tClass);
            return new EsPage<T>(startPage, pageSize, Math.toIntExact(queryForPage.getTotalElements()), queryForPage.getContent());
        }
    }


    /**
     * 分页查询高亮显示
     *
     * @param pageNum  第n页
     * @param pageSize 每页数量
     * @param query    指定查询内容
     * @param tClass   指定类
     * @param fields   指定高亮显示的属性
     * @return
     */
    public Page<T> pageQueryWithHighLight(Integer pageNum, Integer pageSize, String query, Class<T> tClass, String... fields) {
        //分页
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        //google的色值
        String preTag = "<font color='#dd4b39'>";
        String postTag = "</font>";

        //添加查询的字段内容
        QueryBuilder mutiQueryBuilder = QueryBuilders.multiMatchQuery(query, fields);


        List<HighlightBuilder.Field> hignlightBuilderList = new ArrayList<>();
        for (String field : fields) {
            hignlightBuilderList.add(new HighlightBuilder.Field(field).preTags(preTag).postTags(postTag));
        }
        HighlightBuilder.Field[] hignlightBuilderArray = new HighlightBuilder.Field[fields.length];
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(mutiQueryBuilder).
                withHighlightFields(
                        hignlightBuilderList.toArray(hignlightBuilderArray)
                ).
                withPageable(pageable).build();

        AggregatedPage<T> list = elasticsearchTemplate.queryForPage(searchQuery, tClass, new SearchResultMapper() {
            @SneakyThrows
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                List<T> result = new ArrayList<>();

                SearchHits hits = searchResponse.getHits();
                for (SearchHit searchHit : hits) {
                    if (hits.getHits().length <= 0) {
                        return null;
                    }


                    //设置高亮的属性
                    T t = JSON.parseObject(JSON.toJSONString(searchHit.getSourceAsMap()), aClass);
                    for (String f : fields) {
                        HighlightField nameHighlight = searchHit.getHighlightFields().get(f);
                        if (nameHighlight != null) {
                            BeanUtil.setValue(t, aClass, f, aClass.getDeclaredField(f).getType(), nameHighlight.fragments()[0].toString());
                        } else {
                            //没有高亮的name
                            String name = (String) searchHit.getSourceAsMap().get(f);
                            BeanUtil.setValue(t, aClass, f, aClass.getDeclaredField(f).getType(), name);
                        }
                    }
                    result.add(t);
                }

                if (result.size() > 0) {
                    return new AggregatedPageImpl<>((List<T>) result);
                }

                return null;
            }
        });

        return list;

    }


}
