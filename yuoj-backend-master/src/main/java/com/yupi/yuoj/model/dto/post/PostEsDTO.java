package com.yupi.yuoj.model.dto.post;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yupi.yuoj.model.entity.Post;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * ES DTO for solution search
 */
@Document(indexName = "post")
@Data
public class PostEsDTO implements Serializable {

    private static final Gson GSON = new Gson();

    @Id
    private Long id;

    private Long questionId;
    private Long userId;
    private String title;
    private String content;
    private List<String> tags;
    private Integer thumbNum;
    private Integer favourNum;
    private Integer status;
    @Field(index = true, store = true, type = FieldType.Long)
    private Long createTime;

    @Field(index = true, store = true, type = FieldType.Long)
    private Long updateTime;
    private Integer isDelete;

    public static PostEsDTO objToDto(Post post) {
        if (post == null) {
            return null;
        }
        PostEsDTO dto = new PostEsDTO();
        BeanUtils.copyProperties(post, dto);
        if (post.getCreateTime() != null) {
            dto.setCreateTime(post.getCreateTime().getTime());
        }
        if (post.getUpdateTime() != null) {
            dto.setUpdateTime(post.getUpdateTime().getTime());
        }
        String tagsStr = post.getTags();
        if (StringUtils.isNotBlank(tagsStr)) {
            dto.setTags(GSON.fromJson(tagsStr, new TypeToken<List<String>>() {
            }.getType()));
        }
        return dto;
    }

    public static Post dtoToObj(PostEsDTO dto) {
        if (dto == null) {
            return null;
        }
        Post post = new Post();
        BeanUtils.copyProperties(dto, post);
        if (dto.getCreateTime() != null) {
            post.setCreateTime(new Date(dto.getCreateTime()));
        }
        if (dto.getUpdateTime() != null) {
            post.setUpdateTime(new Date(dto.getUpdateTime()));
        }
        if (CollectionUtils.isNotEmpty(dto.getTags())) {
            post.setTags(GSON.toJson(dto.getTags()));
        }
        return post;
    }

    private static final long serialVersionUID = 1L;
}
