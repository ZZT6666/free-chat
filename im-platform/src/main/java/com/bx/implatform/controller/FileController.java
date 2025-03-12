package com.bx.implatform.controller;

import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.thirdparty.FileService;
import com.bx.implatform.vo.FileVO;
import com.bx.implatform.vo.GroupVO;
import com.bx.implatform.vo.UploadImageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "文件上传")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传图片", description = "上传图片,上传后返回原图和缩略图的url")
    @PostMapping("/image/upload")
    public Result<UploadImageVO> uploadImage(@RequestParam("file") MultipartFile file) {
        return ResultUtils.success(fileService.uploadImage(file));
    }

    @CrossOrigin
    @Operation(summary = "上传文件", description = "上传文件，上传后返回文件url")
    @PostMapping("/file/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        return ResultUtils.success(fileService.uploadFile(file), "");
    }
    @CrossOrigin
    @Operation(summary = "下载文件", description = "下载文件，下载后返回文件url")
    @PostMapping("/file/download")
    public Result<String> downloadFile(@RequestParam("file") MultipartFile file) {
        return ResultUtils.success(fileService.uploadFile(file), "");
    }
    @Operation(summary = "查询所有文件", description = "查询所有文件")
    @GetMapping("/file/list")
    public Result<List<FileVO>> getFileList() {
        return ResultUtils.success(fileService.getFileList());
    }
}
