package com.example.test.pojo.dto;

import com.example.test.common.enums.UpdateUserType;
import lombok.Data;

@Data
public class UpdateUserinfoDTO {
    private UpdateUserType type;
    private Object data;
}
