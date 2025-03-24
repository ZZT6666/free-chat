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
import com.bx.implatform.util.BeanUtils;
import com.bx.implatform.util.UserStateUtils;
import com.bx.implatform.vo.GroupMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
        groupSession.setGroupId(dto.getGroupId());
        groupSession.setCreatorId(session.getUserId());
        groupSession.setCreateTime(System.currentTimeMillis());
        groupSession.setParticipants(new HashMap<>());

        // 初始化参与者
        dto.getUserInfos().forEach(user -> {
            verifyGroupMember(dto.getGroupId(), user.getUserId());
            groupSession.getParticipants().put(user.getUserId(),
                    new WebrtcGroupSession.Participant(user.getUserId(), user.getTerminal()));
        });

        // 保存会话
        saveGroupSession(groupSession);

        // 发送群组通话邀请
        sendGroupCallMessage(dto.getGroupId(), session, dto.getUserInfos());
    }

    @Override
    public void accept(Long groupId) {
        UserSession session = SessionContext.getSession();
        log.info("接受群组通话,groupId:{},uid:{}", groupId, session.getUserId());

        WebrtcGroupSession groupSession = getGroupSession(groupId);
        updateParticipantStatus(groupSession, session.getUserId(), true);

        // 广播接受通知
        broadcastGroupMessage(groupId, MessageType.RTC_GROUP_ACCEPT, "已加入通话");
    }

    @Override
    public void reject(Long groupId) {
        UserSession session = SessionContext.getSession();
        log.info("拒绝群组通话,groupId:{},uid:{}", groupId, session.getUserId());

        WebrtcGroupSession groupSession = getGroupSession(groupId);
        updateParticipantStatus(groupSession, session.getUserId(), false);

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
        broadcastGroupMessage(dto.getGroupId(), MessageType.RTC_GROUP_FAILED, dto);
    }

    @Override
    public void join(WebrtcGroupJoinDTO dto) {
        UserSession session = SessionContext.getSession();
        log.info("加入群组通话,groupId:{},uid:{}", dto.getGroupId(), session.getUserId());

        WebrtcGroupSession groupSession = getGroupSession(dto.getGroupId());
        groupSession.getParticipants().put(session.getUserId(),
                new WebrtcGroupSession.Participant(session.getUserId(), session.getTerminal()));

        saveGroupSession(groupSession);
        broadcastGroupMessage(dto.getGroupId(), MessageType.RTC_GROUP_JOIN, "已加入房间");
    }

    @Override
    public void invite(WebrtcGroupInviteDTO dto) {
        UserSession session = SessionContext.getSession();
        log.info("邀请加入群组通话,groupId:{},uid:{}", dto.getGroupId(), session.getUserId());

        WebrtcGroupSession groupSession = getGroupSession(dto.getGroupId());
        dto.getUserInfos().forEach(user -> {
            verifyGroupMember(dto.getGroupId(), user.getUserId());
            groupSession.getParticipants().putIfAbsent(user.getUserId(),
                    new WebrtcGroupSession.Participant(user.getUserId(), user.getTerminal()));
        });

        saveGroupSession(groupSession);
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
        groupSession.getParticipants().remove(session.getUserId());

        if (!groupSession.getParticipants().isEmpty()) {
            saveGroupSession(groupSession);
            broadcastGroupMessage(groupId, MessageType.RTC_GROUP_QUIT, "已退出通话");
        } else {
            deleteGroupSession(groupId);
            broadcastGroupMessage(groupId, MessageType.RTC_GROUP_END, "通话已结束");
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
        broadcastGroupMessage(dto.getGroupId(), MessageType.RTC_GROUP_DEVICE, dto);
    }

    @Override
    public void heartbeat(Long groupId) {
        String key = getGroupSessionKey(groupId);
        redisTemplate.expire(key, 60, TimeUnit.SECONDS);
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

    private void saveGroupSession(WebrtcGroupSession session) {
        String key = getGroupSessionKey(session.getGroupId());
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
        messageInfo.setType(MessageType.RTC_GROUP_CALL.code());
        messageInfo.setGroupId(groupId);
        messageInfo.setSendId(sender.getUserId());
        messageInfo.setContent("视频通话邀请");

        IMGroupMessage<GroupMessageVO> message = new IMGroupMessage<>();
        message.setSender(new IMUserInfo(sender.getUserId(), sender.getTerminal()));
        message.setGroupId(groupId);
        message.setData(messageInfo);

        Set<Integer> terminals = new HashSet<>();
        targets.forEach(user -> terminals.add(user.getTerminal()));
        message.setRecvTerminals(new ArrayList<>(terminals));

        imClient.sendGroupMessage(message);
    }

    private void broadcastGroupMessage(Long groupId, MessageType type, Object content) {
        GroupMessageVO messageInfo = new GroupMessageVO();
        messageInfo.setType(type.code());
        messageInfo.setGroupId(groupId);
        messageInfo.setContent(content);

        IMGroupMessage<GroupMessageVO> message = new IMGroupMessage<>();
        message.setGroupId(groupId);
        message.setData(messageInfo);
        imClient.sendGroupMessage(message);
    }

    private void sendTargetMessage(Long groupId, Long targetUid, MessageType type, String content) {
        UserSession session = SessionContext.getSession();
        WebrtcGroupSession groupSession = getGroupSession(groupId);

        GroupMessageVO messageInfo = new GroupMessageVO();
        messageInfo.setType(type.code());
        messageInfo.setGroupId(groupId);
        messageInfo.setSendId(session.getUserId());
        messageInfo.setContent(content);

        IMGroupMessage<GroupMessageVO> message = new IMGroupMessage<>();
        message.setSender(new IMUserInfo(session.getUserId(), session.getTerminal()));
        message.setGroupId(groupId);
        message.setData(messageInfo);

        WebrtcGroupSession.Participant participant = groupSession.getParticipants().get(targetUid);
        if (participant != null) {
            message.setRecvTerminals(Collections.singletonList(participant.getTerminal()));
        }

        imClient.sendGroupMessage(message);
    }

    private void updateParticipantStatus(WebrtcGroupSession session, Long userId, boolean accepted) {
        WebrtcGroupSession.Participant participant = session.getParticipants().get(userId);
        if (participant != null) {
            participant.setAccepted(accepted);
            saveGroupSession(session);
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
