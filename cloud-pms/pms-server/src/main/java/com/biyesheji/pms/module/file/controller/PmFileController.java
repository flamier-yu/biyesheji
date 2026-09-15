package com.biyesheji.pms.module.file.controller;

import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.common.enums.BusinessType;
import com.biyesheji.pms.framework.annotation.Log;
import com.biyesheji.pms.module.file.entity.PmAttachment;
import com.biyesheji.pms.module.file.service.IPmAttachmentService;
import com.biyesheji.pms.module.file.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文件管理
 */
@Slf4j
@Tag(name = "14. 文件管理", description = "附件上传、下载、删除与秒传校验")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class PmFileController {

    private final IPmAttachmentService attachService;
    private final StorageService storageService;

    @Operation(summary = "上传附件")
    @Log(title = "文件管理", businessType = BusinessType.INSERT, saveResponseData = false)
    @PostMapping("/upload")
    public R<PmAttachment> upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(required = false) String bizType,
                                  @RequestParam(required = false) Long bizId) {
        return R.ok(attachService.upload(file, bizType, bizId));
    }

    @Operation(summary = "查询业务对象下的附件")
    @GetMapping("/list")
    public R<List<PmAttachment>> list(@RequestParam String bizType, @RequestParam Long bizId) {
        return R.ok(attachService.listByBiz(bizType, bizId));
    }

    @Operation(summary = "MD5 秒传校验")
    @GetMapping("/check")
    public R<PmAttachment> check(@RequestParam String md5) {
        return R.ok(attachService.findByMd5(md5));
    }

    @Operation(summary = "下载附件")
    @GetMapping("/download/{attachId}")
    public void download(@PathVariable Long attachId, HttpServletResponse response) throws IOException {
        PmAttachment attachment = attachService.getAttachment(attachId);
        byte[] data = storageService.read(attachment.getFilePath());

        String fileName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8.name())
                .replace("+", "%20");
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName);
        response.setContentLength(data.length);
        response.getOutputStream().write(data);
        response.getOutputStream().flush();
    }

    @Operation(summary = "删除附件")
    @Log(title = "文件管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{attachId}")
    public R<Void> remove(@PathVariable Long attachId) {
        return R.toAjax(attachService.deleteAttachment(attachId));
    }
}
