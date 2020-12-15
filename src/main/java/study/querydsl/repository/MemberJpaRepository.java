package study.querydsl.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    /* public MemberJpaRepository(EntityManager em, JPAQueryFactory queryFactory) {
        this.em = em;
        this.queryFactory = queryFactory;
    } */

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findAll_Querydsl() {
        QMember member = QMember.member;
        return queryFactory.selectFrom(member).fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username).getResultList();
    }

    public List<Member> findByUsername_Querydsl(String username) {
        QMember member = QMember.member;
        return queryFactory.selectFrom(member).where(member.username.eq(username)).fetch();
    }
    
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        BooleanBuilder builder = new BooleanBuilder();
        if ( StringUtils.hasText(condition.getUsername()) ) {
            builder.and(member.username.eq(condition.getUsername()));
        }
        if ( StringUtils.hasText(condition.getTeamName()) ) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

        List<MemberTeamDto> result = queryFactory
                .select(new QMemberTeamDto(
                    member.id.as("memeberId"),
                    member.username,
                    member.age,
                    team.id.as("teamId"),
                    team.name.as("teamName")
                    ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();

        return result;
    }

    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        List<MemberTeamDto> result = queryFactory
                .select(new QMemberTeamDto(
                                member.id.as("memeberId"),
                                member.username,
                                member.age,
                                team.id.as("teamId"),
                                team.name.as("teamName")
                                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();

        return result;
    }

    private BooleanExpression usernameEq(String username) {
        QMember member = QMember.member;
        return StringUtils.isEmpty(username) ? null : member.username.eq(username);
    }

    private BooleanExpression teamNameEq(String teamName) {
        QTeam team = QTeam.team;
        return StringUtils.isEmpty(teamName) ? null : team.name.eq(teamName);
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        QMember member = QMember.member;
        return ageGoe == null ? null : member.age.goe(ageGoe);
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        QMember member = QMember.member;
        return ageLoe == null ? null : member.age.loe(ageLoe);
    }

    // where 파라미터 방식은 이런식으로 재사용이 가능하다.
    public List<Member> findMember(MemberSearchCondition condition) {
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        return queryFactory
                        .selectFrom(member)
                        .leftJoin(member.team, team)
                        .where(
                            usernameEq(condition.getUsername()),
                            teamNameEq(condition.getTeamName()),
                            ageGoe(condition.getAgeGoe()),
                            ageLoe(condition.getAgeLoe())
                        )
                        .fetch();
    }

}
