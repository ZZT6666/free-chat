package com.bx.implatform.util;

import com.bx.implatform.config.props.MinioProperties;
import com.bx.implatform.vo.FileVO;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioClient minioClient;
    private  final MinioProperties minioProps;

    /**
     * 查看存储bucket是否存在
     *
     * @return boolean
     */
    public Boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        } catch (Exception e) {
            log.error("查询bucket失败", e);
            return false;
        }
    }

    /**
     * 创建存储bucket
     */
    public void makeBucket(String bucketName) {
        try {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
        } catch (Exception e) {
            log.error("创建bucket失败,", e);
        }
    }

    /**
     * 设置bucket权限为public
     */
    public void setBucketPublic(String bucketName) {
        try {
            // 设置公开
            String sb = "{\"Version\":\"2012-10-17\"," +
                    "\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":" +
                    "{\"AWS\":[\"*\"]},\"Action\":[\"s3:ListBucket\",\"s3:ListBucketMultipartUploads\"," +
                    "\"s3:GetBucketLocation\"],\"Resource\":[\"arn:aws:s3:::" + bucketName +
                    "\"]},{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:PutObject\",\"s3:AbortMultipartUpload\",\"s3:DeleteObject\",\"s3:GetObject\",\"s3:ListMultipartUploadParts\"],\"Resource\":[\"arn:aws:s3:::" +
                    bucketName +
                    "/*\"]}]}";
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(sb)
                            .build());
        } catch (Exception e) {
            log.error("创建bucket失败,", e);
        }
    }

    /**
     * 文件上传
     *
     * @param bucketName bucket名称
     * @param path       路径
     * @param file       文件
     * @return Boolean
     */
    public String upload(String bucketName, String path, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            throw new RuntimeException();
        }
        String fileName = System.currentTimeMillis() + "";
        if (originalFilename.lastIndexOf(".") >= 0) {
            fileName += originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectName = DateTimeUtils.getFormatDate(new Date(), DateTimeUtils.PARTDATEFORMAT) + "/" + fileName;
        try {
            InputStream stream = new ByteArrayInputStream(file.getBytes());
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(bucketName).object(path + "/" + objectName)
                .stream(stream, file.getSize(), -1).contentType(file.getContentType()).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            log.error("上传图片失败,", e);
            return null;
        }
        return objectName;
    }

    /**
     * 文件上传
     *
     * @param bucketName  bucket名称
     * @param path        路径
     * @param name        文件名
     * @param fileByte    文件内容
     * @param contentType contentType
     * @return objectName
     */
    public String upload(String bucketName, String path, String name, byte[] fileByte, String contentType) {

        String fileName = System.currentTimeMillis() + name.substring(name.lastIndexOf("."));
        String objectName = DateTimeUtils.getFormatDate(new Date(), DateTimeUtils.PARTDATEFORMAT) + "/" + fileName;
        try {
            InputStream stream = new ByteArrayInputStream(fileByte);
            PutObjectArgs objectArgs = PutObjectArgs.builder().bucket(bucketName).object(path + "/" + objectName)
                    .stream(stream, fileByte.length, -1).contentType(contentType).build();
            //文件名称相同会覆盖
            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            log.error("上传文件失败,", e);
            return null;
        }
        return objectName;
    }



    /**
     * 删除
     *
     * @param bucketName bucket名称
     * @param path       路径
     * @param fileName   文件名
     * @return true/false
     */
    public boolean remove(String bucketName, String path, String fileName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(path + fileName).build());
        } catch (Exception e) {
            log.error("删除文件失败,", e);
            return false;
        }
        return true;
    }

    /**
     * 获取所有文件列表（包含file和image文件夹）
     *
     * @param bucketName bucket名称
     * @return 文件列表
     */
    public List<FileVO> getAllFilesList(String bucketName) {
        List<FileVO> fileVOList = new ArrayList<>();
        // 获取file文件夹下的文件
        fileVOList.addAll(getFileList(bucketName, "file/"));
        // 获取image文件夹下的文件
        fileVOList.addAll(getFileList(bucketName, "image/"));
        return fileVOList;
    }

    /**
     * 获取指定路径下的文件列表
     *
     * @param bucketName bucket名称
     * @param path       路径
     * @return 文件列表
     */
    public List<FileVO> getFileList(String bucketName, String path) {
        List<FileVO> fileVOList = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(path)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();

                // 跳过文件夹本身
                if (objectName.equals(path)) {
                    continue;
                }

                FileVO fileVO = new FileVO();
                fileVO.setBucketName(bucketName);

                // 去除路径前缀，只保留文件名
                int lastSlashIndex = objectName.lastIndexOf('/');
                String fileName = lastSlashIndex == -1 ? objectName : objectName.substring(lastSlashIndex + 1);
                fileVO.setFileName(fileName);
                fileVO.setUpdateTime(convertZonedDateTimeToDate(item.lastModified()));
                fileVO.setFileSize(formatFileSize(item.size()));
                fileVO.setUrl(minioProps.getDomain() + "/" + minioProps.getBucketName()+ "/" + objectName);
                // 设置文件类型（0表示file，1表示image）
                fileVO.setType(path.startsWith("file/") ? 0 : 1);
                fileVOList.add(fileVO);
            }
        } catch (Exception e) {
            log.error("获取文件列表失败，路径：" + path, e);
        }
        return fileVOList;
    }

    /**
     * 格式化文件大小
     *
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
    private Date convertZonedDateTimeToDate(ZonedDateTime zonedDateTime) {
        return Date.from(zonedDateTime.toInstant());
    }
    /**
     * 文件下载
     *
     * @param bucketName bucket名称
     * @param path       路径
     * @param bucketName 文件名称
     * @param path       下载路径
     * @return true/false
     */
    public boolean downloadFile(String bucketName, String path, String fileName, Path downloadPath) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(path + fileName).build())) {
            Files.copy(stream, downloadPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            log.error("文件下载失败", e);
            return false;
        }
        return true;
    }

}
