package com.bx.implatform.controller;

import com.bx.implatform.annotation.RepeatSubmit;
import com.bx.implatform.entity.Group;
import com.bx.implatform.result.Result;
import com.bx.implatform.result.ResultUtils;
import com.bx.implatform.service.GroupService;
import com.bx.implatform.vo.GroupInviteVO;
import com.bx.implatform.vo.GroupMemberVO;
import com.bx.implatform.vo.GroupVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "群聊")
@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @RepeatSubmit
    @Operation(summary = "创建群聊", description = "创建群聊")
    @PostMapping("/create")
    public Result<GroupVO> createGroup(@Valid @RequestBody GroupVO vo) {
        return ResultUtils.success(groupService.createGroup(vo));
    }
    @RepeatSubmit
    @Operation(summary = "创建群聊", description = "创建群聊")
    @PostMapping("/admin/create")
    public Result<GroupVO> adminCreateGroup(@Valid @RequestBody GroupVO vo) {
        return ResultUtils.success(groupService.adminCreateGroup(vo));
    }


    @Operation(summary = "修改群聊信息", description = "修改群聊信息")
    @PutMapping("/modify")
    public Result<GroupVO> modifyGroup(@Valid @RequestBody GroupVO vo) {
        return ResultUtils.success(groupService.modifyGroup(vo));
    }

    @Operation(summary = "解散群聊", description = "解散群聊")
    @DeleteMapping("/delete/{groupId}")
    public Result deleteGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        groupService.deleteGroup(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "查询群聊", description = "查询单个群聊信息")
    @GetMapping("/find/{groupId}")
    public Result<GroupVO> findGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        return ResultUtils.success(groupService.findById(groupId));
    }

    @Operation(summary = "查询群聊列表", description = "查询群聊列表")
    @GetMapping("/list")
    public Result<List<GroupVO>> findGroups() {
        return ResultUtils.success(groupService.findGroups());
    }

    @Operation(summary = "查询所有群聊列表", description = "查询群聊列表")
    @GetMapping("/groupList")
    public Result<List<Group>> findAllGroups() {
        return ResultUtils.success(groupService.findAllGroups());
    }

    @Operation(summary = "邀请进群", description = "邀请好友进群")
    @PostMapping("/invite")
    public Result invite(@Valid @RequestBody GroupInviteVO vo) {
        groupService.invite(vo);
        return ResultUtils.success();
    }
    @Operation(summary = "添加进群", description = "添加好友进群")
    @PostMapping("/admin/addMember")
    public Result addMember(@Valid @RequestBody GroupInviteVO vo) {
        groupService.addMember(vo);
        return ResultUtils.success();
    }
    @Operation(summary = "设置群主", description = "设置群主")
    @PostMapping("/setOwner/{groupId}")
    public Result setOwner(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId,
                           @NotNull(message = "用户id不能为空") @RequestParam Long userId) {
        groupService.setOwner(groupId, userId);
        return ResultUtils.success();
    }
    @Operation(summary = "设置管理员", description = "设置管理员")
    @PostMapping("/setAdmin/{groupId}")
    public Result setAdmin(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId,
                            @NotNull(message = "用户id不能为空") @RequestParam Long userId) {
        groupService.setAdmin(groupId, userId);
        return ResultUtils.success();
    }
    @Operation(summary = "取消管理员", description = "取消管理员")
    @PostMapping("/cancelAdmin/{groupId}")
    public Result cancelAdmin(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId,
                           @NotNull(message = "用户id不能为空") @RequestParam Long userId) {
        groupService.cancelAdmin(groupId, userId);
        return ResultUtils.success();
    }

    @Operation(summary = "查询群聊成员", description = "查询群聊成员")
    @GetMapping("/members/{groupId}")
    public Result<List<GroupMemberVO>> findGroupMembers(
        @NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        return ResultUtils.success(groupService.findGroupMembers(groupId));
    }
    @Operation(summary = "查询群聊成员", description = "查询群聊成员")
    @GetMapping("/admin/members/{groupId}")
    public Result<List<GroupMemberVO>> adminFindGroupMembers(
            @NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        return ResultUtils.success(groupService.adminFindGroupMembers(groupId));
    }
    @Operation(summary = "退出群聊", description = "退出群聊")
    @DeleteMapping("/quit/{groupId}")
    public Result quitGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId) {
        groupService.quitGroup(groupId);
        return ResultUtils.success();
    }

    @Operation(summary = "踢出群聊", description = "将用户踢出群聊")
    @DeleteMapping("/kick/{groupId}")
    public Result kickGroup(@NotNull(message = "群聊id不能为空") @PathVariable Long groupId,
        @NotNull(message = "用户id不能为空") @RequestParam Long userId) {
        groupService.kickGroup(groupId, userId);
        return ResultUtils.success();
    }

}

