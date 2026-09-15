package com.biyesheji.pms.module.message.controller;

import com.biyesheji.pms.common.core.PageResult;
import com.biyesheji.pms.common.core.R;
import com.biyesheji.pms.module.message.entity.PmMessage;
import com.biyesheji.pms.module.message.service.IPmMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 消息中心
 */
@Tag(name = "15. 消息中心", description = "站内消息查询、已读标记与删除")
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class PmMessageController {

    private final IPmMessageService messageService;

    @Operation(summary = "分页查询我的消息")
    @GetMapping("/list")
    public R<PageResult<PmMessage>> list(PmMessage message,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        return R.ok(messageService.selectMessagePage(message, pageNum, pageSize));
    }

    @Operation(summary = "查询未读消息数量")
    @GetMapping("/unread/count")
    public R<Integer> unreadCount() {
        return R.ok(messageService.countUnread());
    }

    @Operation(summary = "未读消息按类型统计")
    @GetMapping("/unread/types")
    public R<List<Map<String, Object>>> unreadTypes() {
        return R.ok(messageService.countUnreadByType());
    }

    @Operation(summary = "标记消息为已读（支持批量）")
    @PutMapping("/read")
    public R<Void> read(@RequestBody PmMessage message) {
        return R.toAjax(messageService.readByIds(message.getMessageIds()));
    }

    @Operation(summary = "全部标记为已读")
    @PutMapping("/readAll")
    public R<Void> readAll() {
        return R.toAjax(messageService.readAll());
    }

    @Operation(summary = "删除消息（支持批量）")
    @DeleteMapping("/{messageIds}")
    public R<Void> remove(@PathVariable Long[] messageIds) {
        return R.toAjax(messageService.deleteByIds(messageIds));
    }
}
