package com.bx.implatform.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UpdataPwdDTO {
    private Integer id;
    @NotEmpty(message = "旧密码不可为空")
    @Schema(description = "旧密码")
    private String oldPassword;
    @NotEmpty(message = "新用户密码不可为空")
    @Schema(description = "新用户密码")
    private String newPassword;
}
