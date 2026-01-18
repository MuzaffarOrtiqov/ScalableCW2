package api.vybe.controller;

import api.vybe.dto.video.VideoResponseDto;
import api.vybe.dto.video.VideoUploadDto;
import api.vybe.entity.VideoEntity;
import api.vybe.enums.VideoStatus;
import api.vybe.service.VideoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/videos", produces = MediaType.APPLICATION_JSON_VALUE)
//@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVideo(
            @RequestPart("video") MultipartFile file,
            @RequestPart("data") String dataJson) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            VideoUploadDto request = mapper.readValue(dataJson, VideoUploadDto.class);

            VideoResponseDto response = videoService.uploadVideo(file, request);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }


/*    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadVideo(
            @RequestPart("video") MultipartFile file,
            @RequestPart("data") VideoUploadDto request) {
        try {
            VideoResponseDto response = videoService.uploadVideo(file, request);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload video: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }*/

    @GetMapping
    public ResponseEntity<List<VideoResponseDto>> getAllVideos(
            @RequestParam(required = false) VideoStatus status) {
        List<VideoResponseDto> videos =
                (status == null)
                        ? videoService.getAllVideos()
                        : videoService.getVideosByStatus(status);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{id}/stream") // Add /stream here to differentiate
    public ResponseEntity<Resource> streamVideo(@PathVariable String id) {
        // Get the video metadata to find the filename
        VideoResponseDto video = videoService.getVideoById(id);

        // Load the file from your storage (Azure or Local)
        Resource file = videoService.loadAsResource(video.getOriginalFilename());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + video.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType("video/mp4")) // This forces the correct header
                .body(file);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoResponseDto> getVideoById(@PathVariable String id) {
        VideoResponseDto video = videoService.getVideoById(id);
        return ResponseEntity.ok(video);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VideoResponseDto> updateVideo(
            @PathVariable String id,
            @RequestBody VideoUploadDto request) {
        VideoResponseDto updated = videoService.updateVideo(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVideo(@PathVariable String id) {
        videoService.deleteVideo(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Video deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<?> incrementViews(@PathVariable String id) {
        videoService.incrementViews(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "View count incremented");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> incrementLikes(@PathVariable String id) {
        videoService.incrementLikes(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Like count incremented");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stream/{id}")
    public ResponseEntity<Map<String, String>> getStreamUrl(@PathVariable String id) {
        String streamUrl = videoService.generateStreamUrl(id);

        Map<String, String> response = new HashMap<>();
        response.put("url", streamUrl);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        List<VideoResponseDto> videos = videoService.getAllVideos();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalVideos", videos.size());
        stats.put("totalViews", videos.stream().mapToLong(VideoResponseDto::getViews).sum());
        stats.put("totalLikes", videos.stream().mapToLong(VideoResponseDto::getLikes).sum());

        return ResponseEntity.ok(stats);
    }


}
