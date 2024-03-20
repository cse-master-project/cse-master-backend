package com.example.csemaster.login.user;

import com.example.csemaster.login.user.dto.UserDTO;
import com.example.csemaster.login.user.entity.ActiveUserEntity;
import com.example.csemaster.login.user.entity.UserEntity;
import com.example.csemaster.login.user.entity.UserRefreshTokenEntity;
import com.example.csemaster.login.user.mapper.UserMapper;
import com.example.csemaster.login.user.repository.ActiveUserRepository;
import com.example.csemaster.login.user.repository.UserRefreshTokenRepository;
import com.example.csemaster.login.user.repository.UserRepository;
import com.example.csemaster.login.user.response.UserTokenResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserLoginService {
    private final UserRepository userRepository;
    private final ActiveUserRepository activeUserRepository;
    private final UserRefreshTokenRepository refreshTokenRepository;
    private final UserJwtProvider userJwtProvider;

    @Autowired
    public UserLoginService(UserRepository userRepository, ActiveUserRepository activeUserRepository, UserJwtProvider userJwtProvider, UserRefreshTokenRepository userRefreshTokenRepository) {
        this.userRepository = userRepository;
        this.activeUserRepository = activeUserRepository;
        this.userJwtProvider = userJwtProvider;
        this.refreshTokenRepository = userRefreshTokenRepository;
    }

    public List<UserDTO> getAllUsers() {
        List<UserEntity> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper.INSTANCE::entityToDTO)
                .collect(Collectors.toList());
    }

    public String isGoogleAuth(String accessToken) {
        try {
            // 구글 API에 액세스토큰을 넘겨서 유저의 정보를 확인함
            RestTemplate restTemplate = new RestTemplate();
            String userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo";
            String requestUri = userInfoEndpoint + "?access_token=" + accessToken;
            String response = restTemplate.getForObject(requestUri, String.class);
            System.out.println(response);

            try {
                // 리스폰 받은 json 정보 파싱 후, google id만 반환
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response);

                return jsonNode.get("sub").asText();
            } catch (JsonProcessingException e) {
                return null;
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            // 401이 반환되면 예외가 발생하며, 액세스 토큰의 인증이 실패 했다는 의미
            return null;
        }
    }
    public String getUserId(String googleId) {
        ActiveUserEntity user = activeUserRepository.findByGoogleId(googleId);
        if (user != null) {
            return user.getUserId();
        } else {
            return null;
        }
    }
    public UserTokenResponse getTokens(String userId) {
        UserTokenResponse token = userJwtProvider.generateToken(userId);

        UserRefreshTokenEntity refreshToken = new UserRefreshTokenEntity();
        refreshToken.setUserId(userId);
        refreshToken.setRefreshToken(token.getRefreshToken());
        refreshToken.setIssueAt(token.getIssueAt());
        refreshToken.setExpAt(token.getExpAt());
        refreshTokenRepository.save(refreshToken);

        return token;
    }

    @Transactional
    public void createUser(String googleId, String nickname) {
        UserEntity user = new UserEntity();
        UUID uuid = UUID.randomUUID();
        user.setUserId(uuid.toString());
        user.setIsActive(true);
        userRepository.save(user);

        ActiveUserEntity activeUser = new ActiveUserEntity();
        activeUser.setUserId(uuid.toString());
        activeUser.setGoogleId(googleId);
        activeUser.setNickname(nickname);
        activeUser.setCreateAt(LocalDateTime.now());
        activeUserRepository.save(activeUser);
    }
}
