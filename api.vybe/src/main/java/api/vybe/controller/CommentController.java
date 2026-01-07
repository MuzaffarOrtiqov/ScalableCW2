package api.vybe.controller;

import api.vybe.dto.comment.CommentDTO;
import api.vybe.dto.comment.CommentRequest;
import api.vybe.exps.AppBadException;
import api.vybe.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {
    private final CommentService commentService;
    /**
     * Add a new comment
     * POST /api/comments
     */
    @PostMapping
    public ResponseEntity<CommentDTO> addComment(@Valid @RequestBody CommentRequest request) {
        log.info("Received request to add comment for video: {}", request.getVideoId());

        try {
            CommentDTO comment = commentService.addComment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (Exception e) {
            log.error("Error adding comment: {}", e.getMessage(), e);
            throw new AppBadException("Failed to add comment: " + e.getMessage());
        }
    }

    /**
     * Get all comments for a video
     * GET /api/comments/video/{videoId}
     */
    @GetMapping("/video/{videoId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByVideo(@PathVariable String videoId) {
        log.info("Fetching comments for video: {}", videoId);

        try {
            List<CommentDTO> comments = commentService.getCommentsByVideoId(videoId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            log.error("Error fetching comments: {}", e.getMessage(), e);
            throw new AppBadException("Failed to fetch comments: " + e.getMessage());
        }
    }

    /**
     * Get comment count for a video
     * GET /api/comments/video/{videoId}/count
     */
    @GetMapping("/video/{videoId}/count")
    public ResponseEntity<Map<String, Long>> getCommentCount(@PathVariable String videoId) {
        log.info("Fetching comment count for video: {}", videoId);

        try {
            Long count = commentService.getCommentCount(videoId);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching comment count: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch comment count: " + e.getMessage());
        }
    }

    /**
     * Delete a comment
     * DELETE /api/comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable String commentId) {
        log.info("Deleting comment: {}", commentId);

        try {
            commentService.deleteComment(commentId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Comment deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting comment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete comment: " + e.getMessage());
        }
    }

    /**
     * Like a comment
     * POST /api/comments/{commentId}/like
     */
    @PostMapping("/{commentId}/like")
    public ResponseEntity<CommentDTO> likeComment(@PathVariable String commentId) {
        log.info("Liking comment: {}", commentId);

        try {
            CommentDTO comment = commentService.likeComment(commentId);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            log.error("Error liking comment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to like comment: " + e.getMessage());
        }
    }

    /**
     * Unlike a comment
     * POST /api/comments/{commentId}/unlike
     */
    @PostMapping("/{commentId}/unlike")
    public ResponseEntity<CommentDTO> unlikeComment(@PathVariable String commentId) {
        log.info("Unliking comment: {}", commentId);

        try {
            CommentDTO comment = commentService.unlikeComment(commentId);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            log.error("Error unliking comment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to unlike comment: " + e.getMessage());
        }
    }
}
