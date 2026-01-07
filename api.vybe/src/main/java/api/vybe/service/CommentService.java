package api.vybe.service;

import api.vybe.dto.comment.CommentDTO;
import api.vybe.dto.comment.CommentRequest;
import api.vybe.entity.CommentEntity;
import api.vybe.repository.CommentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;

    /**
     * Add a new comment to a video
     */
    @Transactional
    public CommentDTO addComment(CommentRequest request) {
        log.info("Adding comment for video: {} by user: {}", request.getVideoId(), request.getUsername());

        CommentEntity comment = new CommentEntity();
        comment.setVideoId(request.getVideoId());
        comment.setUsername(request.getUsername());
        comment.setContent(request.getContent());
        comment.setLikes(0);

        CommentEntity savedComment = commentRepository.save(comment);
        log.info("Comment added successfully with ID: {}", savedComment.getId());

        return convertToDTO(savedComment);
    }

    /**
     * Get all comments for a specific video
     */
    public List<CommentDTO> getCommentsByVideoId(String videoId) {
        log.info("Fetching comments for video: {}", videoId);
        List<CommentEntity> comments = commentRepository.findByVideoIdOrderByCreatedAtDesc(videoId);

        return comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get comment count for a video
     */
    public Long getCommentCount(String videoId) {
        return commentRepository.countByVideoId(videoId);
    }

    /**
     * Delete a comment by ID
     */
    @Transactional
    public void deleteComment(String commentId) {
        log.info("Deleting comment with ID: {}", commentId);

        if (!commentRepository.existsById(commentId)) {
            throw new RuntimeException("Comment not found with ID: " + commentId);
        }

        commentRepository.deleteById(commentId);
        log.info("Comment deleted successfully");
    }

    /**
     * Like a comment
     */
    @Transactional
    public CommentDTO likeComment(String commentId) {
        log.info("Liking comment with ID: {}", commentId);

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));

        comment.setLikes(comment.getLikes() + 1);
        CommentEntity updatedComment = commentRepository.save(comment);

        return convertToDTO(updatedComment);
    }

    /**
     * Unlike a comment
     */
    @Transactional
    public CommentDTO unlikeComment(String commentId) {
        log.info("Unliking comment with ID: {}", commentId);

        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));

        if (comment.getLikes() > 0) {
            comment.setLikes(comment.getLikes() - 1);
        }

        CommentEntity updatedComment = commentRepository.save(comment);
        return convertToDTO(updatedComment);
    }

    /**
     * Delete all comments for a video (used when deleting a video)
     */
    @Transactional
    public void deleteCommentsByVideoId(String videoId) {
        log.info("Deleting all comments for video: {}", videoId);
        commentRepository.deleteByVideoId(videoId);
    }

    /**
     * Convert Comment entity to DTO
     */
    private CommentDTO convertToDTO(CommentEntity comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setVideoId(comment.getVideoId());
        dto.setUsername(comment.getUsername());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setLikes(comment.getLikes());
        return dto;
    }
}
