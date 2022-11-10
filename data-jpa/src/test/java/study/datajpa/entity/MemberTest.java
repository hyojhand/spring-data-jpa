package study.datajpa.entity;

import org.assertj.core.api.Assertions;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.repository.MemberRepository;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@DisplayName("Member 테스트")
@Rollback(value = false)
class MemberTest {

    @PersistenceContext EntityManager em;

    @Autowired
    MemberRepository memberRepository;
    @Test
    @DisplayName("엔티티 조회 후 프록시 조회 테스트")
    public void proxyTest() {
        // given
        Member member = new Member("member1", 10);
        em.persist(member);

        em.flush();
        em.clear();

        // when
        Member findMember = em.find(Member.class, member.getId());
        System.out.println("findMember : " + findMember.getClass());

        Member refMember = em.getReference(Member.class, member.getId());
        System.out.println("refMember : " + refMember.getClass());

        // then
        assertThat(findMember.equals(refMember));
    }

    @Test
    @DisplayName("프록시 조회 후 엔티티를 조회")
    public void proxyTest2() {
        // given
        Member member = new Member("member1", 10);
        em.persist(member);

        em.flush();
        em.clear();

        // when
        Member refMember = em.getReference(Member.class, member.getId());
        System.out.println("refMember : " + refMember.getClass());

        Member findMember = em.find(Member.class, member.getId());
        System.out.println("findMember : " + findMember.getClass());

        // then
        assertThat(refMember.equals(findMember));
    }

    @Test
    @DisplayName("프록시 초기화 전 영속성컨텍스트 에러")
    public void proxyTest3() {
        // given
        Member member = new Member("member1", 10);
        em.persist(member);

        em.flush();
        em.clear();

        // when
        Member refMember = em.getReference(Member.class, member.getId());
        System.out.println("refMember : " + refMember.getClass());

        em.detach(refMember);
        // em.close();
        // em.clear();

        // then
        assertThatThrownBy(() -> refMember.getUsername()).isInstanceOf(LazyInitializationException.class);
    }

    @Test
    public void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1",10,teamA);
        Member member2 = new Member("member2",20,teamA);
        Member member3 = new Member("member3",30,teamB);
        Member member4 = new Member("member4",40,teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        em.clear();

        //확인
        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("-> member.team = " + member.getTeam());
        }
    }

    @Test
    public void JpaEventBaseEntity() throws Exception {
        // given
        Member member = new Member("member1");
        memberRepository.save(member); // @PrePersist

        Thread.sleep(100);
        member.setUsername("member2");

        em.flush(); // @PreUpdate
        em.clear();


        //when
        Member findMember = memberRepository.findById(member.getId()).get();

        //then
        System.out.println("findMember.createdDate = " + findMember.getCreatedDate());
        System.out.println("findMember.updateDate = " + findMember.getLastModifiedDate());
        System.out.println("findMember.createdBy = " + findMember.getCreatedBy());
        System.out.println("findMember.updatedBy = " + findMember.getLastModifiedBy());
    }

}