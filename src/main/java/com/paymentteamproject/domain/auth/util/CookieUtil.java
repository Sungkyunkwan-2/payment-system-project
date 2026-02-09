package com.paymentteamproject.domain.auth.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Optional;

/**
 * 쿠키 관련 유틸리티 클래스
 */
@Slf4j
public class CookieUtil {

    private static HttpServletRequest request;
    private static String name;

    /**
     * HttpOnly 쿠키 생성 및 추가
     *
     * @param response HTTP 응답
     * @param name 쿠키 이름
     * @param value 쿠키 값
     * @param maxAge 만료 시간 (초)
     * @param secure HTTPS 전용 여부
     */
    public static void addCookie(
            HttpServletResponse response,
            String name,
            String value,
            int maxAge,
            boolean secure
    ) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie);

        log.debug("쿠키 생성: name={}, maxAge={}, secure={}", name, maxAge, secure);
    }

    /**
     * 쿠키에서 값 추출
     *
     * @param request HTTP 요청
     * @param name 쿠키 이름
     * @return 쿠키 값 (Optional)
     */
    public static Optional<String> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * 쿠키 삭제
     *
     * @param response HTTP 응답
     * @param name 쿠키 이름
     * @param secure HTTPS 전용 여부
     */
    public static void deleteCookie(
            HttpServletResponse response,
            String name,
            boolean secure
    ) {
        Cookie cookie = new Cookie(name, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        log.debug("쿠키 삭제: name={}", name);
    }
}

