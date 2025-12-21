package com.example.test.pojo.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "搜索用户返回的数据格式")
public class StsVO {
    String endpoint;
    String accessKeyId;
    String accessKeySecret;
    String securityToken;
    String region;
    String bucketName;
}
