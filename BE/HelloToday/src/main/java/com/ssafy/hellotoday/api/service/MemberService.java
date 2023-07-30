package com.ssafy.hellotoday.api.service;

import com.ssafy.hellotoday.api.dto.member.response.TokenDto;
import com.ssafy.hellotoday.common.exception.CustomException;
import com.ssafy.hellotoday.db.entity.Member;
import com.ssafy.hellotoday.db.entity.Role;
import com.ssafy.hellotoday.db.entity.Social;
import com.ssafy.hellotoday.db.repository.MemberRepository;
import com.ssafy.hellotoday.jwt.JwtTokenProvider;
import com.ssafy.hellotoday.oauth2.kakao.KakaoMemberDto;
import com.ssafy.hellotoday.oauth2.kakao.KakaoOAuth2;
import com.ssafy.hellotoday.oauth2.naver.NaverMemberDto;
import com.ssafy.hellotoday.oauth2.naver.NaverOAuth2;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
    @Value("${jwt.secretKey}")
    private String secretKey;

    private final KakaoOAuth2 kakaoOAuth2;
    private final NaverOAuth2 naverOAuth2;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;

    private final RedisTemplate<String, String> redisTemplate;
    // authorizedCode로 가입된 사용자 조회
    @Transactional
    public Member findKakaoMemberByAuthorizedCode(String authorizedCode, String redirectUri) {
        // 카카오 OAuth2 를 통해 카카오 사용자 정보 조회
        KakaoMemberDto kakaoUserDto = kakaoOAuth2.getMemberInfo(authorizedCode, redirectUri);
        String email = kakaoUserDto.getEmail();

        String socialId = kakaoUserDto.getSocialId();
        Optional<Member> optionalMember = memberRepository.findBySocialId(socialId);

        if(optionalMember.isPresent()) return optionalMember.get();
            // 가입된 유저가 아니라면 회원가입 진행
        else {
            String name = kakaoUserDto.getName();
            String profilePath = kakaoUserDto.getProfilePath();

            Member member = Member.builder()
                    .role(Role.USER)
                    .email(email)
                    .nickname(name)
                    .profilePath(profilePath)
                    .socialId(socialId)
                    .socialType(Social.KAKAO)
                    .build();

            return memberRepository.save(member);
        }
    }

    @Transactional
    public Member findNaverMemberByAuthorizedCode(String authorizedCode, String naverState) {
        // 카카오 OAuth2 를 통해 카카오 사용자 정보 조회
        NaverMemberDto naverMemberDto = naverOAuth2.getMemberInfo(authorizedCode, naverState);
        String email = naverMemberDto.getEmail();

        String socialId = naverMemberDto.getSocialId();
        Optional<Member> optionalMember = memberRepository.findBySocialId(socialId);

        if(optionalMember.isPresent()) return optionalMember.get();
            // 가입된 유저가 아니라면 회원가입 진행
        else {
            String name = naverMemberDto.getName();
            String profilePath = naverMemberDto.getProfilePath();

            Member member = Member.builder()
                    .role(Role.USER)
                    .email(email)
                    .nickname(name)
                    .profilePath(profilePath)
                    .socialId(socialId)
                    .socialType(Social.NAVER)
                    .build();

            return memberRepository.save(member);
        }
    }

    @Transactional(readOnly = true)
    public Member findMemberByJwtToken(String token) {
        String id = String.valueOf(Jwts.parser().setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token).getBody().get("id"));
        System.out.println("id = " + id);
        return memberRepository.findById(Integer.parseInt(id))
                .orElseThrow(() -> new IllegalArgumentException("이메일 \"" + 1 + "\"에 해당하는 사용자가 존재하지 않습니다."));
    }

    @Transactional
    public TokenDto reissue(String refreshToken) {

        log.info("재발급서비스 진입!!!");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, 403, "토큰에 문제 생겼어요");
        }

        String id = Jwts.parser().setSigningKey(secretKey.getBytes())
                .parseClaimsJws(refreshToken).getBody().getId();


        Member findMember = memberRepository.findById(Integer.parseInt(id))
                .orElse(null);
        if (findMember == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, -1, "일치하는 유저가 없습니다");
        }

        String redisRefreshToken = redisTemplate.opsForValue().get(Integer.toString(findMember.getMemberId()));

        if (!refreshToken.equals(redisRefreshToken)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, -1, "refresh Token 불일치");
        }
        String newAccessToken = jwtTokenProvider.createAccessToken(findMember.getMemberId(), findMember.getEmail());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(findMember.getMemberId());

        jwtTokenProvider.storeRefreshToken(findMember.getMemberId(),newRefreshToken);
        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(newAccessToken);
        tokenDto.setRefreshToken(newRefreshToken);
        return tokenDto;
    }
}
