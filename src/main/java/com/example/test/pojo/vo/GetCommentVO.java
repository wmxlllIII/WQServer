package com.example.test.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "分页获取帖子评论返回数据类型")
public class GetCommentVO {
    private int id;
    private int postId;
    private String userId;
    private String userName;
    private int parentId;
    private String replyToUserId;
    private String replyToUserName;
    private String content;
    private long createAt;
    private List<GetCommentVO> childCommentList;
}
