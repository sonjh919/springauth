package com.sparta.springauth.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Spring Security 지원을 가능하게 함
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { // 인가 처리, 옵션 설정 등 수행
        // CSRF 설정
        http.csrf((csrf) -> csrf.disable());

        http.authorizeHttpRequests((authorizeHttpRequests) ->
            authorizeHttpRequests
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // resources 폴더 접근 허용 설정
//                .requestMatchers("/api/user/**").permitAll() // 허용 설정, 다양한 케이스들이 있음
                .anyRequest().authenticated() // 그 외 모든 요청 인증처리
        );

        // 로그인 사용
        http.formLogin(Customizer.withDefaults()); // security에서 default login 기능 제공 // id : user, password : 실행될때마다 달라짐
        // session id가 security 내부에 저장되어 인증 처리를 해줌
        // security는 기본적으로 session 방식으로 작동된다.

        return http.build();
    }
}