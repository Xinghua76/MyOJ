package com.yupi.yuoj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.constant.CommonConstant;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.exception.ThrowUtils;
import com.yupi.yuoj.mapper.PostMapper;
import com.yupi.yuoj.model.dto.post.PostQueryRequest;
import com.yupi.yuoj.model.entity.Post;
import com.yupi.yuoj.model.entity.PostFavour;
import com.yupi.yuoj.model.entity.PostThumb;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.PostVO;
import com.yupi.yuoj.model.vo.UserVO;
import com.yupi.yuoj.mapper.PostFavourMapper;
import com.yupi.yuoj.mapper.PostThumbMapper;
import com.yupi.yuoj.service.PostService;
import com.yupi.yuoj.service.UserService;
import com.yupi.yuoj.utils.SqlUtils;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Solution service (reusing Post module)
 */
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import com.yupi.yuoj.model.dto.post.PostEsDTO;
import java.util.ArrayList;

@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    @Resource
    private UserService userService;

    @Resource
    private PostThumbMapper postThumbMapper;

    @Resource
    private PostFavourMapper postFavourMapper;

    @Override
    public void validPost(Post post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = post.getTitle();
        String content = post.getContent();
        String tags = post.getTags();
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        Long userId = postQueryRequest.getUserId();
        Long notId = postQueryRequest.getNotId();
        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollectionUtils.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "user_id", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Autowired(required = false)
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public Page<Post> searchFromEs(PostQueryRequest postQueryRequest) {
        if (elasticsearchRestTemplate == null) {
            return this.page(new Page<>(postQueryRequest.getCurrent(), postQueryRequest.getPageSize()),
                    this.getQueryWrapper(postQueryRequest));
        }
        Long id = postQueryRequest.getId();
        Long notId = postQueryRequest.getNotId();
        String searchText = postQueryRequest.getSearchText();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        List<String> orTagList = postQueryRequest.getOrTags();
        Long userId = postQueryRequest.getUserId();
        // es 起始页为 0
        long current = postQueryRequest.getCurrent() - 1;
        long pageSize = postQueryRequest.getPageSize();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        // 必须包含所有标签
        if (CollectionUtils.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
            }
        }
        // 包含任何一个标签即可
        if (CollectionUtils.isNotEmpty(orTagList)) {
            BoolQueryBuilder orTagBoolQueryBuilder = QueryBuilders.boolQuery();
            for (String tag : orTagList) {
                orTagBoolQueryBuilder.should(QueryBuilders.termQuery("tags", tag));
            }
            orTagBoolQueryBuilder.minimumShouldMatch(1);
            boolQueryBuilder.filter(orTagBoolQueryBuilder);
        }
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("tags", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按标题检索
        if (StringUtils.isNotBlank(title)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", title));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按内容检索
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            if (CommonConstant.SORT_ORDER_ASC.equals(sortOrder)) {
                sortBuilder.order(SortOrder.ASC);
            } else {
                sortBuilder.order(SortOrder.DESC);
            }
        }
        // 分页
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest).withSorts(sortBuilder).build();
        SearchHits<PostEsDTO> searchHits;
        try {
            searchHits = elasticsearchRestTemplate.search(searchQuery, PostEsDTO.class);
        } catch (Exception e) {
            log.warn("es search failed, fallback to db, reason: {}", e.getMessage());
            return this.page(new Page<>(postQueryRequest.getCurrent(), postQueryRequest.getPageSize()),
                    this.getQueryWrapper(postQueryRequest));
        }

        boolean hasSearchCondition = StringUtils.isNotBlank(searchText)
                || StringUtils.isNotBlank(title)
                || StringUtils.isNotBlank(content)
                || CollectionUtils.isNotEmpty(tagList)
                || CollectionUtils.isNotEmpty(orTagList)
                || userId != null
                || id != null
                || notId != null;
        if (hasSearchCondition && searchHits.getTotalHits() == 0) {
            return this.page(new Page<>(postQueryRequest.getCurrent(), postQueryRequest.getPageSize()),
                    this.getQueryWrapper(postQueryRequest));
        }
        Page<Post> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Post> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        if (searchHits.hasSearchHits()) {
            List<SearchHit<PostEsDTO>> searchHitList = searchHits.getSearchHits();
            List<Long> postIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
            List<Post> postList = baseMapper.selectBatchIds(postIdList);
            if (postList != null) {
                Map<Long, List<Post>> idPostMap = postList.stream().collect(Collectors.groupingBy(Post::getId));
                postIdList.forEach(postId -> {
                    if (idPostMap.containsKey(postId)) {
                        resourceList.add(idPostMap.get(postId).get(0));
                    } else {
                        // 从 es 清空 db 已删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(postId), PostEsDTO.class);
                        log.info("delete post {}", delete);
                    }
                });
            }
        }
        page.setRecords(resourceList);
        return page;
    }

    @Override
    public PostVO getPostVO(Post post, HttpServletRequest request) {
        PostVO postVO = PostVO.objToVo(post);
        Long userId = post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PostThumb> thumbQueryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            thumbQueryWrapper.eq("post_id", post.getId());
            thumbQueryWrapper.eq("user_id", loginUser.getId());
            postVO.setHasThumb(postThumbMapper.selectCount(thumbQueryWrapper) > 0);

            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PostFavour> favourQueryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            favourQueryWrapper.eq("post_id", post.getId());
            favourQueryWrapper.eq("user_id", loginUser.getId());
            postVO.setHasFavour(postFavourMapper.selectCount(favourQueryWrapper) > 0);
        }
        if (postVO.getHasThumb() == null) {
            postVO.setHasThumb(false);
        }
        if (postVO.getHasFavour() == null) {
            postVO.setHasFavour(false);
        }
        return postVO;
    }

    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollectionUtils.isEmpty(postList)) {
            return postVOPage;
        }
        Set<Long> userIdSet = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        User loginUser = userService.getLoginUserPermitNull(request);
        Set<Long> thumbPostIds = new HashSet<>();
        Set<Long> favourPostIds = new HashSet<>();
        if (loginUser != null) {
            Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(postIdSet)) {
                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PostThumb> thumbQueryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                thumbQueryWrapper.in("post_id", postIdSet);
                thumbQueryWrapper.eq("user_id", loginUser.getId());
                List<PostThumb> postThumbList = postThumbMapper.selectList(thumbQueryWrapper);
                thumbPostIds = postThumbList.stream().map(PostThumb::getPostId).collect(Collectors.toSet());

                com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PostFavour> favourQueryWrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
                favourQueryWrapper.in("post_id", postIdSet);
                favourQueryWrapper.eq("user_id", loginUser.getId());
                List<PostFavour> postFavourList = postFavourMapper.selectList(favourQueryWrapper);
                favourPostIds = postFavourList.stream().map(PostFavour::getPostId).collect(Collectors.toSet());
            }
        }
        Set<Long> finalThumbPostIds = thumbPostIds;
        Set<Long> finalFavourPostIds = favourPostIds;
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = PostVO.objToVo(post);
            Long userId = post.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));
            postVO.setHasThumb(finalThumbPostIds.contains(post.getId()));
            postVO.setHasFavour(finalFavourPostIds.contains(post.getId()));
            return postVO;
        }).collect(Collectors.toList());
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }
}
