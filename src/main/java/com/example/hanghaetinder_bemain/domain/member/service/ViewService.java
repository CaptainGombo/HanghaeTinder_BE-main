package com.example.hanghaetinder_bemain.domain.member.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.hanghaetinder_bemain.domain.member.dto.resoponse.MemberResponseDto;
import com.example.hanghaetinder_bemain.domain.member.entity.DislikeMember;
import com.example.hanghaetinder_bemain.domain.member.entity.Member;
import com.example.hanghaetinder_bemain.domain.member.repository.DislikeMemberRepository;
import com.example.hanghaetinder_bemain.domain.member.repository.LikeMemberRepository;
import com.example.hanghaetinder_bemain.domain.member.repository.MemberFavoriteRepository;
import com.example.hanghaetinder_bemain.domain.member.repository.MemberRepository;
import com.example.hanghaetinder_bemain.domain.security.UserDetailsImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewService {

	private final MemberRepository memberRepository;
	private final MemberFavoriteRepository memberFavoriteRepository;
	private final LikeMemberRepository likeMemberRepository;
	private final DislikeMemberRepository dislikeMemberRepository;

	@Transactional(readOnly = true)
	public List<MemberResponseDto> users(UserDetailsImpl userDetails) {
		//1.사용자 아이디를꺼낸다
		Long userId = userDetails.getId();
		//2.사용자를 멤버객체에넣고
		Member member = findMemberById(userId);
		//3. 싫어요를 누르거나 눌려진
		List<Long> dislikesUserId = dislikeUser(member);
		//4. 좋아요를 누르거나 눌려진
		List<Long> likesUserId = likeUser(member);
		//5. 3번과 4번을포함 하지 않는 일반유저를 찾는다
		List<Member> normalUsers = new ArrayList<>();
		for (Member user : memberRepository.findAll()) {
			if (!dislikesUserId.contains(user.getId()) && !likesUserId.contains(user.getId())&& !user.getId().equals(userId)) {
				normalUsers.add(user);
			}
		}
		List<MemberResponseDto> result = new ArrayList<>();
		for (Member members : normalUsers) {
			List<Long> favoriteList = memberFavoriteRepository.findByFavoriteList1(member.getId());
			result.add(new MemberResponseDto(members, favoriteList));
		}
		//7. 리스트를 랜덤하게 섞는다
		Collections.shuffle(result);
		return result;
	}

	//사용자를 좋아요누른사람 목록
	@Transactional(readOnly = true)
	public List<MemberResponseDto> likedUser(UserDetailsImpl userDetails){
		//1.사용자 아이디를꺼낸다
		Long userId = userDetails.getId();

		//2.사용자를 멤버객체에넣고
		Member member = findMemberById(userId);

		//3. 나를 좋아요 누른사람을찾는다
		List<Member> likeMemberToUser = likeMemberRepository.findAllByLikedMember(member.getId());

		//4. 나를누른사람의 user id 추출
		List<Long> likeByUserIds = new ArrayList<>();
		for (Member like : likeMemberToUser) {
			likeByUserIds.add(like.getId());
		}
		//5. 반환값
		List<MemberResponseDto> result = new ArrayList<>();
		for (Long id : likeByUserIds) {
			Member matchedMember = memberRepository.findById(id).orElseThrow(
				() -> new IllegalArgumentException("사용자를 찾을수 없습니다."));
			List<Long> favoriteList = memberFavoriteRepository.findByFavoriteList1(matchedMember.getId());
			result.add(new MemberResponseDto(matchedMember, favoriteList));
		}
		//6.랜덤으로섞는다
		Collections.shuffle(result);
		return result;

	}

	public Member findMemberById(Long id){
		return memberRepository.findById(id).orElseThrow(
			() -> new IllegalArgumentException("사용자를 찾을수가 없습니다"));
	}

	private List<Long> dislikeUser(Member member){
		//3. 내가 싫어요를 누른사람과
		List<DislikeMember> dislikesByUser = dislikeMemberRepository.findAllByMember(member);
		//4. 나를 싫어요 누른사람을 담는다
		List<DislikeMember> dislikesToUser = dislikeMemberRepository.findAllByDislikeMember(member.getId());
		//7. 3번과 4번을 충족하는 유저id를 한곳에 넣어주고
		List<Long> dislikeUserIds = new ArrayList<>();
		for (DislikeMember dislike : dislikesByUser) {
			dislikeUserIds.add(dislike.getMember().getId());
		}
		for (DislikeMember dislike : dislikesToUser) {
			dislikeUserIds.add(dislike.getMember().getId());
		}
		return dislikeUserIds;
	}

	private List<Long> likeUser(Member member){
		//내가 좋아요를 누른사람
		List<Member> likesByUser = likeMemberRepository.findAllByMember(member.getId());
		//나를 좋아요를 누른사람
		List<Member> likesToUser = likeMemberRepository.findAllByLikedMember(member.getId());
		List<Long> likeUserIds = new ArrayList<>();
		for (Member like : likesByUser) {
			likeUserIds.add(like.getId());
		}
		for (Member like : likesToUser) {
			likeUserIds.add(like.getId());
		}
		return likeUserIds;
	}
}