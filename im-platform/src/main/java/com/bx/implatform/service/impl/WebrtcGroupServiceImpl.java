package com.bx.implatform.service.impl;

import com.bx.imclient.IMClient;
import com.bx.imcommon.model.IMGroupMessage;
import com.bx.imcommon.model.IMUserInfo;
import com.bx.implatform.contant.RedisKey;
import com.bx.implatform.dto.*;
import com.bx.implatform.entity.GroupMessage;
import com.bx.implatform.enums.MessageStatus;
import com.bx.implatform.enums.MessageType;
import com.bx.implatform.exception.GlobalException;
import com.bx.implatform.service.GroupMemberService;
import com.bx.implatform.service.GroupMessageService;
import com.bx.implatform.service.WebrtcGroupService;
import com.bx.implatform.session.SessionContext;
import com.bx.implatform.session.UserSession;
import com.bx.implatform.session.WebrtcGroupSession;
import com.bx.implatform.session.WebrtcUserInfo;
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.util.UserStateUtils;
import com.bx.implatform.vo.GroupMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebrtcGroupServiceImpl implements WebrtcGroupService {

    private final IMClient imClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final GroupMemberService groupMemberService;
    private final GroupMessageService groupMessageService;
    private final UserStateUtils userStateUtils;

    @Override
    public void setup(WebrtcGroupSetupDTO dto) {
        UserSession session = SessionContext.getSession();
        log.info("创建群组通话,sid:{},groupId:{}", session.getUserId(), dto.getGroupId());

        // 验证群组成员身份
        verifyGroupMember(dto.getGroupId(), session.getUserId());

        // 创建群组会话
        WebrtcGroupSession groupSession = new WebrtcGroupSession();
        // 发起者
        groupSession.setHost(getSessionUserInfo(session));
        // 被邀请用户列表
        groupSession.setUserInfos(dto.getUserInfos());
        // 已经加入聊天的用户列表
        List<IMUserInfo> inChatUsers = new ArrayList<>();
        inChatUsers.add(getSessionUserInfo(session));
        groupSession.setInChatUsers(inChatUsers);

        // 保存会话
        saveGroupSession(groupSession,dto.getGroupId());

        // 发送群组通话邀请
        sendGroupCallMessage(dto.getGroupId(), session, dto.getUserInfos());
    }

    @Override
    public void accept(Long groupId) {
        UserSession session = SessionContext.getSession();
        log.info("接受群组通话,groupId:{},uid:{}", groupId, session.getUserId());

        WebrtcGroupSession groupSession = getGroupSession(groupId);
        //更新通话中用户列表
        updateInChatUsers(groupSession, getSessionUserInfo(session), groupId);

        // 广播接受通知
        broadcastGroupMessage(groupId, MessageType.RTC_GROUP_ACCEPT, "已加入通话");
    }

    @Override
    public void reject(Long groupId) {
        UserSession session = SessionContext.getSession();
        log.info("拒绝群组通话,groupId:{},uid:{}", groupId, session.getUserId());

        WebrtcGroupSession groupSession = getGroupSession(groupId);
        //更新通话中用户列表
        updateInChatUsers(groupSession, getSessionUserInfo(session), groupId);

        // 广播拒绝通知
        broadcastGroupMessage(groupId, MessageType.RTC_GROUP_REJECT, "已拒绝加入");
    }

    @Override
    public void failed(WebrtcGroupFailedDTO dto) {
        UserSession session = SessionContext.getSession();
        log.info("群组通话失败,groupId:{},reason:{}", dto.getGroupId(), dto.getReason());

        // 保存系统消息
        saveSystemMessage(dto.getGroupId(), "通话失败：" + dto.getReason());

        // 广播失败通知
        broadcastGroupMessage(dto.getGroupId(), MessageType.RTC_GROUP_FAILED, dto.getReason());
    }

    @Override
    public void join(WebrtcGroupJoinDTO dto) {
        UserSession session = SessionContext.getSession();
        log.info("加入群组通话,groupId:{},uid:{}", dto.getGroupId(), session.getUserId());

        WebrtcGroupSession groupSession = getGroupSession(dto.getGroupId());

        //更新通话中用户列表
        updateInChatUsers(groupSession, getSessionUserInfo(session), dto.getGroupId());

        broadcastGroupMessage(dto.getGroupId(), MessageType.RTC_GROUP_JOIN, "已加入房间");
    }

    @Override
    public void invite(WebrtcGroupInviteDTO dto) {
        UserSession session = SessionContext.getSession();
        log.info("邀请加入群组通话,groupId:{},uid:{}", dto.getGroupId(), session.getUserId());

        WebrtcGroupSession groupSession = getGroupSession(dto.getGroupId());
        groupSession.getUserInfos().addAll(dto.getUserInfos());

        saveGroupSession(groupSession,dto.getGroupId());
        sendGroupCallMessage(dto.getGroupId(), session, dto.getUserInfos());
    }

    @Override
    public void offer(WebrtcGroupOfferDTO dto) {
        sendTargetMessage(dto.getGroupId(), dto.getUserId(), MessageType.RTC_GROUP_OFFER, dto.getOffer());
    }

    @Override
    public void answer(WebrtcGroupAnswerDTO dto) {
        sendTargetMessage(dto.getGroupId(), dto.getUserId(), MessageType.RTC_GROUP_ANSWER, dto.getAnswer());
    }

    @Override
    public void candidate(WebrtcGroupCandidateDTO dto) {
        sendTargetMessage(dto.getGroupId(), dto.getUserId(), MessageType.RTC_GROUP_CANDIDATE, dto.getCandidate());
    }

    @Override
    public void quit(Long groupId) {
        UserSession session = SessionContext.getSession();
        log.info("退出群组通话,groupId:{},uid:{}", groupId, session.getUserId());

        WebrtcGroupSession groupSession = getGroupSession(groupId);
        List<IMUserInfo> inChatUsers = groupSession.getInChatUsers();
        inChatUsers.remove(getSessionUserInfo(session));
        groupSession.setInChatUsers(inChatUsers);

        if (groupSession.getInChatUsers().size() >= 2) {
            saveGroupSession(groupSession,groupId);
            broadcastGroupMessage(groupId, MessageType.RTC_GROUP_QUIT, "已退出通话");
        } else {
            deleteGroupSession(groupId);
        }
    }

    @Override
    public void cancel(Long groupId) {
        deleteGroupSession(groupId);
        broadcastGroupMessage(groupId, MessageType.RTC_GROUP_CANCEL, "通话已取消");
        saveSystemMessage(groupId, "视频通话已取消");
    }

    @Override
    public void device(WebrtcGroupDeviceDTO dto) {
        broadcastGroupMessage(dto.getGroupId(), MessageType.RTC_GROUP_DEVICE, String.valueOf(dto));
    }

    @Override
    public void heartbeat(Long groupId) {
        // 会话续命
        String key = getGroupSessionKey(groupId);
        redisTemplate.expire(key, 60, TimeUnit.SECONDS);
        // 用户状态续命
        UserSession session = SessionContext.getSession();
        userStateUtils.expire(session.getUserId());
    }
    private IMUserInfo getSessionUserInfo(UserSession session) {
        //获取当前用户信息
        IMUserInfo user = new IMUserInfo();
        user.setId(session.getUserId());
        user.setTerminal(session.getTerminal());
        return user;
    }
    private void verifyGroupMember(Long groupId, Long userId) {
        if (!groupMemberService.isInGroup(groupId, Collections.singletonList(userId))) {
            throw new GlobalException("用户不在群组中");
        }
    }

    private WebrtcGroupSession getGroupSession(Long groupId) {
        String key = getGroupSessionKey(groupId);
        WebrtcGroupSession session = (WebrtcGroupSession) redisTemplate.opsForValue().get(key);
        if (session == null) {
            throw new GlobalException("群组通话已结束");
        }
        return session;
    }

    private void saveGroupSession(WebrtcGroupSession session,Long groupId) {
        String key = getGroupSessionKey(groupId);
        redisTemplate.opsForValue().set(key, session, 60, TimeUnit.SECONDS);
    }

    private void deleteGroupSession(Long groupId) {
        redisTemplate.delete(getGroupSessionKey(groupId));
    }

    private String getGroupSessionKey(Long groupId) {
        return RedisKey.IM_WEBRTC_GROUP_SESSION + groupId;
    }

    private void sendGroupCallMessage(Long groupId, UserSession sender, List<WebrtcUserInfo> targets) {
        GroupMessageVO messageInfo = new GroupMessageVO();
        messageInfo.setType(MessageType.RTC_GROUP_INVITE.code());
        messageInfo.setGroupId(groupId);
        messageInfo.setSendId(sender.getUserId());
        messageInfo.setContent("视频通话邀请");

        IMGroupMessage<GroupMessageVO> message = new IMGroupMessage<>();
        //发送者
        message.setSender(getSessionUserInfo(sender));
        //接收者
        List<Long> members = targets.stream().map(WebrtcUserInfo::getId).collect(Collectors.toList());
        message.setRecvIds(members);

        message.setData(messageInfo);

        imClient.sendGroupMessage(message);
    }

    private void broadcastGroupMessage(Long groupId, MessageType type, String content) {
        GroupMessageVO messageInfo = new GroupMessageVO();
        messageInfo.setType(type.code());
        messageInfo.setGroupId(groupId);
        messageInfo.setContent(content);

        IMGroupMessage<GroupMessageVO> message = new IMGroupMessage<>();
        //接收者
        List<Long> members = groupMemberService.findUserIdsByGroupId(groupId);
        message.setRecvIds(members);

        message.setData(messageInfo);
        imClient.sendGroupMessage(message);
    }

    private void sendTargetMessage(Long groupId, Long targetUid, MessageType type, String content) {
        UserSession session = SessionContext.getSession();

        GroupMessageVO messageInfo = new GroupMessageVO();
        messageInfo.setType(type.code());
        messageInfo.setGroupId(groupId);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(content);

        IMGroupMessage<GroupMessageVO> message = new IMGroupMessage<>();
        message.setSender(getSessionUserInfo(session));
        //接收者
        message.setRecvIds(Collections.singletonList(targetUid));

        message.setData(messageInfo);

        imClient.sendGroupMessage(message);
    }

    private void updateInChatUsers(WebrtcGroupSession session, IMUserInfo user, Long groupId) {
        List<IMUserInfo> inChatUsers = session.getInChatUsers();
        if (inChatUsers != null) {
            inChatUsers.add(user);
            saveGroupSession(session,groupId);
        }
    }

    private void saveSystemMessage(Long groupId, String content) {
        GroupMessage message = new GroupMessage();
        message.setGroupId(groupId);
        message.setContent(content);
        message.setType(MessageType.TIP_TEXT.code());
        message.setSendTime(new Date());
        message.setStatus(MessageStatus.READED.code());
        groupMessageService.save(message);
    }
}
