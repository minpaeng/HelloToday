package com.ssafy.hellotoday.db.repository;

import com.ssafy.hellotoday.db.entity.Member;
import com.ssafy.hellotoday.db.entity.Social;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Page<Member> findByNicknameStartingWithOrderByNickname(String nickname, Pageable pageable);

    Optional<Member> findByEmail(String email);

    Optional<Member> findBySocialId(String socialId);

    Optional<Member> findByEmailAndSocialType(String email, Social socialType);

    Optional<Member> findByNickname(String nickname);


}
