package com.biyesheji.pms.framework.security;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.biyesheji.pms.common.constant.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 令牌服务
 * <p>
 * JWT 只承载一个随机令牌标识（不存业务数据），真实用户信息存在 Redis 中。
 * 这样做的好处：① 令牌体积小；② 支持服务端主动注销；③ 权限变更可即时生效。
 */
@Slf4j
@Component
public class TokenService {

    /** JWT 中存放令牌标识的声明名 */
    private static final String CLAIM_LOGIN_KEY = "loginKey";

    @Value("${pms.jwt.secret}")
    private String secret;

    @Value("${pms.jwt.expire-minutes:120}")
    private long expireMinutes;

    @Value("${pms.jwt.header:Authorization}")
    private String header;

    @Value("${pms.jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 创建令牌
     */
    public String createToken(LoginUser loginUser) {
        String tokenId = IdUtil.fastSimpleUUID();
        loginUser.setToken(tokenId);
        refreshToken(loginUser);

        Map<String, Object> claims = new HashMap<>(2);
        claims.put(CLAIM_LOGIN_KEY, tokenId);
        return createJwt(claims);
    }

    /**
     * 刷新令牌有效期并回写 Redis
     */
    public void refreshToken(LoginUser loginUser) {
        long now = System.currentTimeMillis();
        loginUser.setLoginTime(now);
        loginUser.setExpireTime(now + expireMinutes * 60 * 1000L);
        String key = getCacheKey(loginUser.getToken());
        redisTemplate.opsForValue().set(key, loginUser, expireMinutes, TimeUnit.MINUTES);
    }

    /**
     * 从请求中解析并取出登录用户
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        String token = resolveToken(request);
        if (StrUtil.isBlank(token)) {
            return null;
        }
        try {
            Claims claims = parseJwt(token);
            String tokenId = String.valueOf(claims.get(CLAIM_LOGIN_KEY));
            if (StrUtil.isBlank(tokenId) || "null".equals(tokenId)) {
                return null;
            }
            String key = getCacheKey(tokenId);
            Object cache = redisTemplate.opsForValue().get(key);
            return cache instanceof LoginUser ? (LoginUser) cache : null;
        } catch (Exception e) {
            // 令牌非法或已过期，按未登录处理
            log.debug("解析令牌失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从请求头中提取令牌
     */
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(header);
        if (StrUtil.isNotBlank(bearer)) {
            if (StrUtil.isNotBlank(tokenPrefix) && bearer.startsWith(tokenPrefix)) {
                return bearer.substring(tokenPrefix.length()).trim();
            }
            return bearer.trim();
        }
        return request.getParameter("token");
    }

    /**
     * 删除令牌（登出）
     */
    public void deleteToken(String tokenId) {
        if (StrUtil.isNotBlank(tokenId)) {
            redisTemplate.delete(getCacheKey(tokenId));
        }
    }

    /**
     * 令牌是否即将过期（30 分钟内），用于自动续期
     */
    public boolean needRefresh(LoginUser loginUser) {
        return loginUser.getExpireTime() != null
                && loginUser.getExpireTime() - System.currentTimeMillis() <= 30 * 60 * 1000L;
    }

    /* ========== 内部方法 ========== */

    private String getCacheKey(String tokenId) {
        return Constants.LOGIN_USER_KEY + tokenId;
    }

    private String createJwt(Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireMinutes * 60 * 1000L);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(buildSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseJwt(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(buildSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey buildSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
