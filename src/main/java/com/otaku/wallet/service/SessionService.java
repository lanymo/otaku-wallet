package com.otaku.wallet.service;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 사용자 세션 관리 서비스
 * 첫 방문 시 자동으로 고유 사용자 ID를 생성하고 세션에 저장
 */
@Slf4j
@Service
public class SessionService {

    private static final String USER_ID_KEY = "userId";

    /**
     * 현재 세션에서 사용자 ID를 가져옴
     * 세션에 userId가 없으면 새로 생성하여 저장
     *
     * @param session HTTP 세션
     * @return 사용자 고유 ID (UUID)
     */
    public String getUserId(HttpSession session) {
        String userId = (String) session.getAttribute(USER_ID_KEY);

        if (userId == null) {
            // 첫 방문 시 UUID 생성
            userId = UUID.randomUUID().toString();
            session.setAttribute(USER_ID_KEY, userId);
            log.info("New user created with ID: {}", userId);
        }

        return userId;
    }

    /**
     * 세션에 사용자 ID가 존재하는지 확인
     *
     * @param session HTTP 세션
     * @return 사용자 ID 존재 여부
     */
    public boolean hasUserId(HttpSession session) {
        return session.getAttribute(USER_ID_KEY) != null;
    }

    /**
     * 세션을 무효화하고 사용자 ID 삭제 (로그아웃)
     *
     * @param session HTTP 세션
     */
    public void clearSession(HttpSession session) {
        String userId = (String) session.getAttribute(USER_ID_KEY);
        session.invalidate();
        log.info("Session cleared for user ID: {}", userId);
    }
}
