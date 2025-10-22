package com.example.test.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户发布动态返回的数据格式")
public class PostsVO {
    @ApiModelProperty("帖子ID")
    private int postId;

    @ApiModelProperty("用户Email")
    private String userId;

    @ApiModelProperty("用户Avatar")
    private String userAvatarUrl;

    @ApiModelProperty("用户昵称")
    private String nickName;

    @ApiModelProperty("帖子文本内容")
    private String content;

    @ApiModelProperty("帖子封面")
    private String coverUrl;

    @ApiModelProperty("图片URL列表")
    private List<String> imageUrls;

    @ApiModelProperty("喜欢数量")
    private int likeCount;

    @ApiModelProperty("创建时间")
    private long createAt;
}
