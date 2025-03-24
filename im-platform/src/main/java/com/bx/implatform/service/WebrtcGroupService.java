package com.bx.implatform.service;

import com.bx.implatform.dto.*;

public interface WebrtcGroupService {
    void setup(WebrtcGroupSetupDTO dto);

    void accept(Long groupId);

    void reject(Long groupId);

    void failed(WebrtcGroupFailedDTO dto);

    void join(WebrtcGroupJoinDTO dto);

    void invite(WebrtcGroupInviteDTO dto);

    void offer(WebrtcGroupOfferDTO dto);

    void answer(WebrtcGroupAnswerDTO dto);

    void quit(Long groupId);

    void cancel(Long groupId);

    void candidate(WebrtcGroupCandidateDTO dto);

    void device(WebrtcGroupDeviceDTO dto);

    void heartbeat(Long groupId);
}