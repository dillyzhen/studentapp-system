package com.shlms.service;

import com.shlms.dto.LoginRequest;
import com.shlms.dto.TokenResponse;
import com.shlms.entity.User;
import com.shlms.enums.UserRole;
import com.shlms.repository.UserRepository;
import com.shlms.security.JwtTokenProvider;
import com.shlms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

            // Update last login time
            userRepository.findById(userPrincipal.getId()).ifPresent(user -> {
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
            });

            String accessToken = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(userPrincipal.getId());

            UserRole role = UserRole.valueOf(userPrincipal.getRole());

            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getExpirationTime() / 1000)
                    .user(TokenResponse.UserInfo.builder()
                            .id(userPrincipal.getId())
                            .username(userPrincipal.getUsername())
                            .name(userPrincipal.getName())
                            .role(userPrincipal.getRole())
                            .roleDisplayName(role.getDisplayName())
                            .build())
                    .build();

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("用户名或密码错误");
        }
    }

    public TokenResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("无效的刷新令牌");
        }

        String userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("用户不存在"));

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new BadCredentialsException("用户已禁用");
        }

        String newAccessToken = tokenProvider.generateTokenFromUserId(
                user.getId(),
                user.getUsername(),
                user.getRole().name()
        );
        String newRefreshToken = tokenProvider.generateRefreshToken(user.getId());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationTime() / 1000)
                .user(TokenResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .name(user.getName())
                        .role(user.getRole().name())
                        .roleDisplayName(user.getRole().getDisplayName())
                        .build())
                .build();
    }

    public void logout(String token) {
        // In a production environment, you might want to add the token to a blacklist
        // For now, we just clear the security context
        SecurityContextHolder.clearContext();
        log.info("User logged out, token: {}", token.substring(0, Math.min(20, token.length())) + "...");
    }
}
