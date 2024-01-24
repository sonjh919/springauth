package com.sparta.springauth.jwt;

import com.sparta.springauth.entity.UserRoleEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import org.springframework.util.StringUtils;

// util 클래스 : 특정한 매개변수나 파라미터에 대한 작업을 수행하는 메서드들이 존재하는 class
// 다른 객체에 의존하지 않고 하나의 모듈로써 동작하는 class
/*
jwt를 보내는 방법 : 서비스, 상황에 따라 더 좋은 방법은 다르다.
1. Response header에 넣어서 보내기
장점 : 코드 수가 줄어든다.
2. Cookie 객체를 만들어서 token을 담은 다음에 그 cookie를 Response 객체에 담기
장점 : JWT 말고 Cookie 자체의 만료기한 설정 가능, 다른 option 추가 가능, header에 set-cookie라는 이름으로 넘어가면서 자동으로 cookie 저장
 */

@Component
public class JwtUtil {
    // JWT 생성 데이터들
    // Header KEY 값
    public static final String AUTHORIZATION_HEADER = "Authorization";
    // 사용자 권한 값의 KEY
    public static final String AUTHORIZATION_KEY = "auth";
    // Token 식별자
    public static final String BEARER_PREFIX = "Bearer "; // Bearer : Token 앞에 붙일 용어(권장)
    // 토큰 만료시간
    private final long TOKEN_TIME = 60 * 60 * 1000L; // 60분, ms단위

    // SecretKey를 관리하는 방법이 여러 가지가 있다. 그중 가장 최근 방법

    @Value("${jwt.secret.key}") // Base64 Encode 한 SecretKey (properties의 키 가져오기)
    private String secretKey;

    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    // 로그 설정
    // 로깅(logging) : 어플리케이션이 동작하는 동안 프로젝트의 상태나 어떠한 동작 정보를 시간 순으로 기록하는 것
    // slf4j 어노테이션도 있고 ,loggerfactory 방법도 있다.
    public static final Logger logger = LoggerFactory.getLogger("JWT 관련 로그");

    // 딱 한번 받아 오면 되는 값을 사용할 때마다 요청을 새로 호출하는 실수를 방지하기 위해 사용
    // 생성자가 호출되고 객체가 만들어진 다음 실행된다.
    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey); // decode SecretKey
        key = Keys.hmacShaKeyFor(bytes);
    }

    // JWT 토큰 생성
    public String createToken(String username, UserRoleEnum role) {
        Date date = new Date();

        // 모든 정보를 넣을 필요는 없음
        return BEARER_PREFIX +
            Jwts.builder()
                .setSubject(username) // 사용자 식별자값(ID)
                .claim(AUTHORIZATION_KEY, role) // 사용자 권한 key-value // claim :  JWT에 담겨 있는 정보 조각
                .setExpiration(new Date(date.getTime() + TOKEN_TIME)) // 만료 시간
                .setIssuedAt(date) // 발급일
                .signWith(key, signatureAlgorithm) // 암호화 알고리즘
                .compact();
    }

    // 생성된 JWT를 Cookie에 저장
    public void addJwtToCookie(String token, HttpServletResponse res) {
        try {
            token = URLEncoder.encode(token, "utf-8").replaceAll("\\+", "%20"); // Cookie Value 에는 공백이 불가능해서 encoding 진행

            Cookie cookie = new Cookie(AUTHORIZATION_HEADER, token); // Name-Value
            cookie.setPath("/");

            // Response 객체에 Cookie 추가
            res.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage());
        }
    }

    // Cookie에 들어있던 JWT 토큰을 Substring : "Bearer " 제외시키기
    // JWT 토큰 substring
    public String substringToken(String tokenValue) {
        // 공백과 null 확인 && Bearer로 시작하는지
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        logger.error("Not Found Token");
        throw new NullPointerException("Not Found Token");
    }

    // JWT 검증
    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            logger.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            logger.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            logger.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    // JWT 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

}