package com.blogapi.repository;

import com.blogapi.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findBySlug(String slug);

    // Full-text search across title, summary, and content
    @Query("""
            SELECT p FROM Post p
            WHERE p.status = 'PUBLISHED'
            AND (
                LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.summary) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Filter by category
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED' AND p.category.id = :categoryId")
    Page<Post> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    // Filter by tag
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE p.status = 'PUBLISHED' AND t.id = :tagId")
    Page<Post> findByTagId(@Param("tagId") Long tagId, Pageable pageable);

    // Filter by author
    @Query("SELECT p FROM Post p WHERE p.author.id = :authorId")
    Page<Post> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);

    // All published posts
    @Query("SELECT p FROM Post p WHERE p.status = 'PUBLISHED'")
    Page<Post> findAllPublished(Pageable pageable);

    // Increment view count atomically
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // Increment like count atomically
    @Modifying
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void incrementLikeCount(@Param("id") Long id);
}
