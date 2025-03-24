package com.bx.implatform.controller;

import com.bx.implatform.annotation.OnlineCheck;
import com.bx.implatform.dto.*;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.WebrtcGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "群组视频通话")
@RestController
@RequestMapping("/webrtc/group")
@RequiredArgsConstructor
public class WebrtcGroupController {

    private final WebrtcGroupService webrtcGroupService;

    @Operation(summary = "创建群聊视频会话")
    @PostMapping("/setup")
    public Result setup(@Valid @RequestBody WebrtcGroupSetupDTO dto) {
        webrtcGroupService.setup(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "接受视频通话邀请")
    @PostMapping("/accept")
    public Result accept(@RequestParam Long groupId) {
        webrtcGroupService.accept(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "拒绝视频通话邀请")
    @PostMapping("/reject")
    public Result reject(@RequestParam Long groupId) {
        webrtcGroupService.reject(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "通话失败通知")
    @PostMapping("/failed")
    public Result failed(@Valid @RequestBody WebrtcGroupFailedDTO dto) {
        webrtcGroupService.failed(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "加入视频通话")
    @PostMapping("/join")
    public Result join(@Valid @RequestBody WebrtcGroupJoinDTO dto) {
        webrtcGroupService.join(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "邀请用户加入通话")
    @PostMapping("/invite")
    public Result invite(@Valid @RequestBody WebrtcGroupInviteDTO dto) {
        webrtcGroupService.invite(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "发送Offer信息")
    @PostMapping("/offer")
    public Result offer(@Valid @RequestBody WebrtcGroupOfferDTO dto) {
        webrtcGroupService.offer(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "发送Answer信息")
    @PostMapping("/answer")
    public Result answer(@Valid @RequestBody WebrtcGroupAnswerDTO dto) {
        webrtcGroupService.answer(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "退出视频通话")
    @PostMapping("/quit")
    public Result quit(@RequestParam Long groupId) {
        webrtcGroupService.quit(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "取消视频通话")
    @PostMapping("/cancel")
    public Result cancel(@RequestParam Long groupId) {
        webrtcGroupService.cancel(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "发送ICE候选信息")
    @PostMapping("/candidate")
    public Result candidate(@Valid @RequestBody WebrtcGroupCandidateDTO dto) {
        webrtcGroupService.candidate(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "设备状态更新")
    @PostMapping("/device")
    public Result device(@Valid @RequestBody WebrtcGroupDeviceDTO dto) {
        webrtcGroupService.device(dto);
        return ResultUtils.success();
    }

    @Operation(summary = "心跳保持")
    @PostMapping("/heartbeat")
    public Result heartbeat(@RequestParam Long groupId) {
        webrtcGroupService.heartbeat(groupId);
        return ResultUtils.success();
    }
}
