package com.onejane.elasticsearch.service;

import com.onejane.elasticsearch.bean.User;
import com.onejane.elasticsearch.repository.UserRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: springboot-elasticsearch
 * @description: 用户服务实现层
 * @author: OneJane
 * @create: 2020-01-12 13:20
 **/
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public long count() {
        return userRepository.count();
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public void delete(User user) {
        userRepository.delete(user);
    }

    @Override
    public Iterable<User> getAll() {
        return userRepository.findAll();
    }

    /**
     * //根据用户名精确查询用户
     * @param name
     * @return
     */
    @Override
    public List<User> getByName(String name) {
        return userRepository.findUserByName(name);
    }

    /**
     *对用户姓名进行分页查询
     * match和term的区别
     * term进行精确匹配
     * match进行模糊匹配
     */
    @Override
    public Page<User> pageQuery(Integer pageNum, Integer pageSize, String q) {

        //分页
        Pageable pageable = PageRequest.of(pageNum, pageSize);
        //指定查询字段-单字段
        MatchPhraseQueryBuilder queryBuilder = QueryBuilders.matchPhraseQuery("name", q);

        //指定查询字段-多字段
        //MultiMatchQueryBuilder queryBuilder1 = QueryBuilders.multiMatchQuery(q, "name", "address");


        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(queryBuilder)
                .withPageable(pageable)
                .build();
        Page<User> users = userRepository.search(searchQuery);

        return users;
    }


    /**
     * 对用户名和地址 分页查询 + 高亮显示
     * @param pageNum
     * @param pageSize
     * @param q
     * @return
     */
    @Override
    public Page<User> pageQueryWithHighLight(Integer pageNum, Integer pageSize, String q) {
        //分页
        Pageable pageable = PageRequest.of(pageNum, pageSize);

        //google的色值
        String preTag = "<font color='#dd4b39'>";
        String postTag = "</font>";

        //添加查询的字段内容
        String [] fileds = {"name", "address"};
        QueryBuilder mutiQueryBuilder = QueryBuilders.multiMatchQuery(q, fileds);
//        QueryBuilder qb1 = QueryBuilders.termsQuery("字段", "值");
//        QueryBuilder qb2 = QueryBuilders.rangeQuery("字段").gt("值");
//        QueryBuilder qb3 = QueryBuilders.boolQuery().must(qb1).must(qb2);//must链接两个查询条件，or的话使用should()。

        SearchQuery searchQuery = new NativeSearchQueryBuilder().
                withQuery(mutiQueryBuilder).
                withHighlightFields(
                        new HighlightBuilder.Field("name").preTags(preTag).postTags(postTag),
                        new HighlightBuilder.Field("address").preTags(preTag).postTags(postTag)
                ).
                withPageable(pageable).build();

        AggregatedPage<User> users = elasticsearchTemplate.queryForPage(searchQuery, User.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                List<User> result = new ArrayList<>();

                SearchHits hits = searchResponse.getHits();
                for(SearchHit searchHit : hits) {
                    if (hits.getHits().length <= 0) {
                        return null;
                    }

                    User user = new User();

                    //设置ID
                    user.setId(Integer.parseInt(searchHit.getId()));

                    //设置sex
                    Integer sex = (Integer)searchHit.getSourceAsMap().get("sex");
                    user.setSex(sex);

                    //设置高亮的name
                    HighlightField nameHighlight = searchHit.getHighlightFields().get("name");
                    if (nameHighlight != null) {
                        user.setName(nameHighlight.fragments()[0].toString());
                    } else {
                        //没有高亮的name
                        String name = (String)searchHit.getSourceAsMap().get("name");
                        user.setName(name);
                    }

                    //设置高亮的address
                    HighlightField addressHighlight = searchHit.getHighlightFields().get("address");
                    if (addressHighlight != null) {
                        user.setAddress(addressHighlight.fragments()[0].toString());
                    } else {
                        //没有高亮的address
                        String address = (String)searchHit.getSourceAsMap().get("address");
                        user.setAddress(address);
                    }

                    result.add(user);
                }

                if (result.size() > 0) {
                    return new AggregatedPageImpl<>((List<T>) result);
                }

                return null;
            }
        });

        return users;

    }
}
