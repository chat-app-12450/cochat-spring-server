package com.sns.project.repository.post;

import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sns.project.controller.post.post.response.QPostSummaryDto;
import com.sns.project.domain.comment.QComment;
import com.sns.project.domain.post.Post;
import com.sns.project.domain.post.QPost;
import com.sns.project.domain.post.QPostLike;
import com.sns.project.domain.follow.QFollow;
import com.sns.project.controller.post.post.response.PostSummaryDto;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostRepositoryImpl extends QuerydslRepositorySupport implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PostRepositoryImpl(JPAQueryFactory queryFactory) {
        super(Post.class);
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<PostSummaryDto> findFollowingPostsOrderedByPopularity(Long userId, Pageable pageable) {
        QPost post = QPost.post;
        QComment comment = QComment.comment;
        QPostLike postLike = QPostLike.postLike;
        QFollow follow = QFollow.follow;

        JPQLQuery<PostSummaryDto> query = queryFactory
            .select(new QPostSummaryDto(
                post.id,
                post.title,
                post.content,
                post.user.name,
                postLike.id.countDistinct(),
                comment.id.countDistinct()
            ))
            .from(post)
            .join(follow).on(post.user.id.eq(follow.following.id))
            .leftJoin(comment).on(comment.post.id.eq(post.id))
            .leftJoin(postLike).on(postLike.post.id.eq(post.id))
            .where(follow.follower.id.eq(userId))
            .groupBy(post.id)
            .orderBy(
                postLike.id.countDistinct().multiply(3)
                    .add(comment.id.countDistinct().multiply(2)).desc()
            );

        List<PostSummaryDto> postSummaries = getQuerydsl().applyPagination(pageable, query).fetch();
        long total = query.fetchCount();

        return new PageImpl<PostSummaryDto>(postSummaries, pageable, total);
    }

    @Override
    public Page<PostSummaryDto> findAllPostsByUser(Long userId, Pageable pageable) {
        QPost post = QPost.post;
        QComment comment = QComment.comment;
        QPostLike postLike = QPostLike.postLike;
        QFollow follow = QFollow.follow;
        
        JPQLQuery<PostSummaryDto> query = queryFactory
            .select(new QPostSummaryDto(
                post.id,
                post.title,
                post.content,
                post.user.name,
                postLike.id.countDistinct(),
                comment.id.countDistinct()
            ))
            .from(post)
            .join(follow).on(post.user.id.eq(follow.following.id))
            .leftJoin(comment).on(comment.post.id.eq(post.id))
            .leftJoin(postLike).on(postLike.post.id.eq(post.id))
            .where(follow.follower.id.eq(userId))
            .groupBy(post.id)
            .orderBy(
                postLike.id.countDistinct().multiply(3)
                    .add(comment.id.countDistinct().multiply(2)).desc()
            );
        
        List<PostSummaryDto> postSummaries = getQuerydsl().applyPagination(pageable, query).fetch();
        long total = query.fetchCount();

        return new PageImpl<PostSummaryDto>(postSummaries, pageable, total);
    }
}
