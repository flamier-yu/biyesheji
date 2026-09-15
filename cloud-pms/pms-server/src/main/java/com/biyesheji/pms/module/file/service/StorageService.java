package com.biyesheji.pms.module.file.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储抽象
 * <p>
 * 当前提供本地磁盘实现（LocalStorageServiceImpl）。
 * 后续如需接入 MinIO / 阿里云 OSS，只需新增一个实现类并调整配置即可，
 * 业务代码依赖的是本接口，不需要改动。
 */
public interface StorageService {

    /**
     * 保存文件
     *
     * @param file    上传文件
     * @param bizType 业务类型（project/task/worklog），用于分目录存储
     * @return 相对存储路径
     */
    String store(MultipartFile file, String bizType);

    /**
     * 读取文件字节
     */
    byte[] read(String path);

    /**
     * 删除文件
     */
    boolean delete(String path);

    /**
     * 对外可访问地址
     */
    String getUrl(String path);

    /**
     * 存储类型标识（local / minio / oss），用于前端或运维识别
     */
    String type();
}
