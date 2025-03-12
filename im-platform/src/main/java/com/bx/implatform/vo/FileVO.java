package com.bx.implatform.vo;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


import java.util.Date;
@Data
public class FileVO {
    private String bucketName;    //  文件夹
    private String fileName;    // 文件名
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;   // 最后修改时间
    private String fileSize;   //  文件大小
    private String url;    //  文件路径
    private Integer type;


}
