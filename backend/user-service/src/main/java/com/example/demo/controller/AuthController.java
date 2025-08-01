package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LogoutRequestDTO;
import com.example.demo.dto.UserCreateRequestDTO;
import com.example.demo.dto.JwtResponseDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.service.AuthService;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtTokenProvider;
import com.example.demo.domain.UserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.TokenService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.core.context.SecurityContextHolder;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import java.util.Map;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.time.Duration;

@RestController
@Tag(name = "Authentication", description = "인증 관련 API")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtProvider;
    private final UserRepository userRepo;
    private final RedisTemplate<String,String> redis;
    private final TokenService tokenService;

    @Operation(summary = "회원가입", description = "이메일/비밀번호로 신규 사용자 등록")
    @PostMapping("/signup")
    public ResponseEntity<UserDTO> register(
            @RequestBody @Valid UserCreateRequestDTO req
    ) {
        UserDTO created = userService.create(req);
        // URI는 실제 UserController의 매핑에 맞추거나, 프론트에서 유저 조회 경로를 알고 있으면 적절히 설정
        return ResponseEntity
                .created(URI.create("/users/" + created.getId()))
                .body(created);
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하고 JWT 발급")
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> login(
            @RequestBody @Valid LoginRequestDTO req,
            HttpServletResponse res
    ) {
        UserEntity user = authService.login(req);
        String role = user.isRoles() ? "ROLE_ADMIN" : "ROLE_USER";

        JwtResponseDTO dto = tokenService.issueTokens(user.getId(), role);
        tokenService.writeTokensAsCookies(dto, res);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "OAuth2 인가 시작", description = "소셜 로그인(provider) 인가 URL로 리다이렉트")
    @GetMapping("/providers/{provider}")
    @ResponseStatus(HttpStatus.FOUND)
    public void redirectToProvider(
            @PathVariable String provider,
            HttpServletResponse response
    ) throws IOException {
        if (!provider.matches("google|kakao")) {
            response.sendError(HttpStatus.BAD_REQUEST.value(),
                               "지원하지 않는 provider: " + provider);
            return;
        }
        response.sendRedirect("/oauth2/authorization/" + provider);
    }

    @Operation(summary = "OAuth2 콜백", description = "소셜 로그인 후 콜백을 받아 JWT 발급")
    @GetMapping("/callback/{provider}")
    public ResponseEntity<JwtResponseDTO> callback(
            @PathVariable String provider,
            Authentication authentication,
            HttpServletResponse res
    ) {
        // UserDetails 객체를 다루는 AuthService 로직에 맞춰 cast/처리
        OAuth2User oauth2 = (OAuth2User) authentication.getPrincipal();
        UserEntity user = authService.processOAuthPostLogin(oauth2);

        String role = user.isRoles() ? "ROLE_ADMIN" : "ROLE_USER";
        JwtResponseDTO dto = tokenService.issueTokens(user.getId(), role);
        tokenService.writeTokensAsCookies(dto, res);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/refresh")
    public JwtResponseDTO refresh(@RequestBody Map<String,String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "refreshToken is required");
        }

        Jws<Claims> claims;
        try {
            claims = jwtProvider.parseToken(refreshToken);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String jti      = claims.getBody().getId();
        String userId   = redis.opsForValue().get(jti);
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        // 액세스 토큰만 재발급
        String email   = claims.getBody().getSubject();
        Long uid = Long.valueOf(userId);
        boolean isAdmin = /* DB 조회 or 파라미터 */ false;
        String role = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        JwtResponseDTO dto = tokenService.issueTokens(uid, role);
        String newAccess = dto.getAccessToken();

        return JwtResponseDTO.builder()
            .accessToken(newAccess)
            .accessExpiresIn(jwtProvider.getAccessExpiryMs())
            .build();
    }



}
