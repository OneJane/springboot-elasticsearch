package com.onejane.elasticsearch.repository;

import com.onejane.elasticsearch.bean.EsEntity;
import com.onejane.elasticsearch.bean.EsPage;
import com.onejane.elasticsearch.bean.User;
import com.onejane.elasticsearch.service.UserService;
import com.onejane.elasticsearch.util.DateUtil;
import com.onejane.elasticsearch.util.ElasticSearchUtil;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.elasticsearch.common.UUIDs;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.*;

/**
 * @program: springboot-elasticsearch
 * @description: 用户接口测试类
 * @author: OneJane
 * @create: 2020-01-12 13:26
 **/
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class UserRepositoryTests {
    @Autowired
    private JestClient jestClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    public ElasticSearchUtil<User> util;


    /**
     * Jest方式：创建索引并添加文档
     */
    @Test
    public void contextLoads() {
        //1、给ES中存储一个文档
        User user = new User();
        user.setId(1);
        user.setName("onejane");
        user.setAddress("苏州市");
        user.setSex(1);

        //构建一个索引功能，指定索引和类型
        Index index = new Index.Builder(user).index("info").type("user").build();

        //执行
        try {
            jestClient.execute(index);
            System.out.println("执行成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Jest方式：查询表达式查询文档
     */
    @Test
    public void search() {
        String queryStr = "{" +
                "    \"query\" : {" +
                "        \"match\" : {" +
                "            \"name\" : \"onejane\"" +
                "        }" +
                "    }" +
                "}";

        Search search = new Search.Builder(queryStr).addIndex("info_jest").addType("user").build();
        try {
            SearchResult result = jestClient.execute(search);
            System.out.println(result.getJsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用repository添加索引及文档
     */
    @Test
    public void addIndex() {
        User user = new User();
        user.setId(10);
        user.setName("onejane");
        user.setAddress("苏州市");
        user.setSex(1);
        //在user类中使用注解表明所处的index和type
        userRepository.save(user);
    }

    /**
     * 使用elasticsearchTemplate添加索引及文档
     */

    @Test
    public void addIndex02() {
        User user = new User();
        user.setId(5);
        user.setName("onejane");
        user.setAddress("苏州市");
        user.setSex(0);

        IndexQuery query = new IndexQueryBuilder().withId(user.getId().toString()).withObject(user).build();
//        IndexQuery query = new IndexQuery();
        query.setIndexName("info_es_template");
        query.setType("user");
        query.setObject(user);
        elasticsearchTemplate.index(query);
    }


    /**
     * 批量新增数据
     */
    @Test
    public void insertData() {
        List<IndexQuery> queryList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            User user = new User();
            user.setId(i);
            user.setName(i % 2 == 0 ? "洗衣机" + i : "空调" + RandomStringUtils.randomAlphanumeric(10));
            user.setAddress(RandomStringUtils.randomAlphanumeric(5));
            user.setSex(new Random().nextInt(10));
            user.setCreateTime(DateUtil.randomDate("1970-01-01 00:00:00","2020-01-01 00:00:00"));
            user.setComment(i % 2 == 0 ? "GitLab系统" + i : "OA办公系统" + RandomStringUtils.randomAlphanumeric(10));
            HashMap<String,String> location = new HashMap<>();
            location.put(RandomStringUtils.randomAlphanumeric(5),RandomStringUtils.randomAlphanumeric(5));
            location.put(RandomStringUtils.randomAlphanumeric(5),RandomStringUtils.randomAlphanumeric(5));
            user.setLocation(location);

            IndexQuery indexQuery =
                    new IndexQueryBuilder()
                            .withId(String.valueOf(user.getId()))
                            .withObject(user)
                            .withIndexName("info_repository")
                            .build();

            queryList.add(indexQuery);

            if (queryList.size() == 1000) {
                this.elasticsearchTemplate.bulkIndex(queryList);
                queryList.clear();
            }
        }

        if (queryList.size() > 0) {
            // 保存剩余数据 (没被1000整除)
            elasticsearchTemplate.bulkIndex(queryList);
        }
    }


    /**
     * 测试索引是否存在
     */
    @Test
    public void isIndexExist() {
        System.out.println(util.isIndexExist("info_repository"));
    }

    /**
     * 创建索引
     */
    @Test
    public void createIndex() {
        System.out.println(util.createIndex("userindex"));
    }


    /**
     * 添加数据
     */
    @Test
    public void addData() throws IOException {
        //创建json文档内容构建器对象
        XContentBuilder content = XContentFactory.jsonBuilder();
        //封装数据
        content.startObject()
                .field("id", "100")
                .field("name", "scott")
                .field("address", "北京")
                .field("sex", 100).endObject();
        util.addData(content, "user", "user", UUIDs.base64UUID());
    }


    /**
     * 批量添加
     */
    @Test
    public void insertBatch() {
        //文档数据
        User user = new User();
        user.setId(5);
        user.setAddress("北京");
        user.setName("独孤想败");
        user.setSex(1);
        //封装文档数据到EsEntity对象中
        EsEntity es01 = new EsEntity();
        es01.setId(UUIDs.base64UUID());
        es01.setData(user);

        User user2 = new User();
        user2.setId(8);
        user2.setAddress("北京");
        user2.setName("独孤求败");
        user2.setSex(0);
        EsEntity es02 = new EsEntity();
        es02.setId(UUIDs.base64UUID());
        es02.setData(user2);


        User user3 = new User();
        user3.setId(9);
        user3.setAddress("不得北京");
        user3.setName("不得不败");
        user3.setSex(1);
        EsEntity es03 = new EsEntity();
        es03.setId(UUIDs.base64UUID());
        es03.setData(user3);


        List<EsEntity> list = new ArrayList<EsEntity>();
        list.add(es01);
        list.add(es02);
        list.add(es03);


        util.insertBatch("user", "user", list);
    }


    /**
     * 删除数据
     */
    @Test
    public void deleteByQuery() throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("id", 5);
        sourceBuilder.query(termQueryBuilder);
        util.deleteByQuery("user", "user", sourceBuilder);

    }

    /**
     * 批量删除
     */
    @Test
    public void deleteBatch() {
        List list = new ArrayList();
        list.add("yOiGmG8BFDgvDSez8Yw4");
        util.deleteBatch("user", "user", list);

    }


    /**
     * userRepository  from+size 分页查询
     */
    @Test
    public void testQueryPage() {

        // 单条件
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(QueryBuilders.matchQuery("name", "李三"))  //对查询条件进行分词，之后再查询 分词
//                .withQuery(QueryBuilders.matchPhraseQuery("name", "刘周健")) //对查询条件不分词，当做一个整体查询 精确匹配
//                .withQuery(QueryBuilders.termQuery("sex", 1)) //精确匹配
//                .withQuery(QueryBuilders.multiMatchQuery("18", "address", "name")) // 多字段查询
                .withQuery(QueryBuilders.wildcardQuery("name", "*空调*"))
//                .withPageable(PageRequest.of(0, 10)) // 分页
                .build();
        searchQuery.addIndices("info_repository");

//        BoolQueryBuilder filter = QueryBuilders.boolQuery();
//        BoolQueryBuilder boolQueryLike = QueryBuilders.boolQuery();
//        QueryBuilder name = QueryBuilders.wildcardQuery("name", "*空调*");
//        boolQueryLike.should(name); // must
//        filter.must(boolQueryLike);
//        SearchQuery searchQuery = new NativeSearchQuery(filter);
//        searchQuery.setPageable(PageRequest.of(0, 10));

        // 多条件
//        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//        if(!HelperUtil.isEmpty(dto.getExtra())){
//            QueryBuilder extraBuilder = QueryBuilders.matchQuery("extra", dto.getExtra());
//            boolQueryBuilder.must(extraBuilder);
//        }
//        SearchQuery searchQuery = new NativeSearchQueryBuilder()
//                .withQuery(boolQueryBuilder)
//                .withPageable(PageRequest.of(dto.getPage()-1, dto.getSize()))
//                .build();
        QueryBuilder queryBuilder= QueryBuilders.boolQuery()
                .must(QueryBuilders.wildcardQuery("name","*洗衣机0*"))
                .must(QueryBuilders.matchQuery("sex",5));

        SearchQuery searchQuery1 = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder).build();

        EsPage<User> dataPage = util.searchDataPage("info_repository","user",1, 2,"", searchQuery1, User.class);

        Page<User> page = userRepository.search(searchQuery1);
        List<User> users = page.getContent();
        for (User user : users) {
            System.out.println(user);
        }

        List<User> userList = userRepository.findUserByNameLike("188");
        for (User user : userList) {
            System.out.println("模糊查询的结果:" + user);
        }

        User user = userRepository.findUserById(3);
        System.out.println("按照id查询的结果：" + user);

        Page<User> user1 = userRepository.findUserByNameOrderByCreateTimeDesc("*洗衣机*", PageRequest.of(1, 10));
        System.out.println("按照id查询分页结果："+user1.getContent().toString());


        // 全文检索
        NativeSearchQueryBuilder QueryBuilder = new NativeSearchQueryBuilder();
        QueryBuilder.withQuery(QueryBuilders.matchQuery("name","空调").operator(Operator.AND)); // 给查询构造器添加条件
        QueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"title","price"},null)); // 结果过滤
        QueryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC)); // 排序
        QueryBuilder.withPageable(PageRequest.of(1,10)); // 分页
        Page<User> result = userRepository.search(QueryBuilder.build());


        // 聚合查询
        NativeSearchQueryBuilder QueryBuilder1 = new NativeSearchQueryBuilder();
        //添加聚合add, 可以聚合多次
        QueryBuilder.addAggregation(AggregationBuilders.terms("popular_brand").field("brand"));
        AggregatedPage<User> result1 = elasticsearchTemplate.queryForPage(QueryBuilder.build(), User.class);
        // 解析聚合
        Aggregations aggregations = result1.getAggregations();
        // 指定名称聚合
        StringTerms agg = aggregations.get("popular_brand");
        List<StringTerms.Bucket> buckets = agg.getBuckets();
        //遍历buckets
        for (StringTerms.Bucket bucket : buckets) {
            System.out.println(bucket.getKeyAsString());
            System.out.println(bucket.getDocCount());
        }
    }


    /**
     * scroll分页查询
     */
    @Test
    public void scroll() {
        Date begin1 = new Date();
        QueryBuilder queryBuilder = QueryBuilders.termQuery("sex", 0);
        SearchQuery searchQuery1 = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .build();
        EsPage<User> dataPage = util.searchDataPage("info_repository", "user", 10001, 5, "", searchQuery1, User.class);//全部查询
        System.out.println("总数：" + dataPage.getRecordCount() + ";查询结果：" + dataPage.getRecordList().toString());
        Date end1 = new Date();
        System.out.println("耗时: " + (end1.getTime() - begin1.getTime()));
    }


    /**
     * 使用elasticsearchTemplate查询用户及高亮查询
     */
    @Test
    public void queryByName() {
        List<User> users = elasticsearchTemplate.queryForList(
                new CriteriaQuery(Criteria.where("name").contains("188")), User.class);

        for (User u : users) {
            System.out.println(u);
        }


        userService.pageQueryWithHighLight(1, 2, "1").forEach(System.out::println);
        util.pageQueryWithHighLight(1, 2, "1", User.class, "name", "address").forEach(System.out::println);
    }


}
