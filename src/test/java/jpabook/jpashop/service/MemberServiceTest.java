package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class MemberServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    @Test
    @Rollback(value = false)
    public void 회원가입() throws Exception {
        Member member = new Member();
        member.setName("memberA");

        Long savedId = memberService.join(member);

        Member findMember = memberRepository.findOne(savedId);
        assertThat(member).isEqualTo(findMember);
    }

    @Test
    public void 중복_회원가입() throws Exception{
        Member member = new Member();
        member.setName("member");
        memberService.join(member);

        Member duplicateMember = new Member();
        duplicateMember.setName("member");

        assertThatThrownBy(() -> memberService.join(duplicateMember))
                .isInstanceOf(IllegalStateException.class);
    }
}