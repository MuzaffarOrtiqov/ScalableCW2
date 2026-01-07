package api.vybe.service;

import api.vybe.controller.AzureBlobService;
import api.vybe.dto.video.VideoResponseDto;
import api.vybe.dto.video.VideoUploadDto;
import api.vybe.entity.VideoEntity;
import api.vybe.enums.VideoStatus;
import api.vybe.exps.AppBadException;
import api.vybe.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final AzureBlobService azureBlobService;
    private final VideoRepository videoRepository;
    private final String uploadDir = "D:\\uploads/videos/";


    public VideoResponseDto uploadVideo(
            MultipartFile file,
            VideoUploadDto request) throws IOException {

        // 1️⃣ Upload video to Azure Blob Storage
        String videoBlobUrl = azureBlobService.uploadVideo(file);
        System.out.println(azureBlobService.generateReadSasUrl(videoBlobUrl));
        // 2️⃣ Create Video entity (metadata only)
        VideoEntity video = new VideoEntity();
        video.setId(UUID.randomUUID().toString()+getExtension(file));
        video.setTitle(request.getTitle());
        video.setCaption(request.getCaption());
        video.setLocation(request.getLocation());
        video.setCategory(request.getCategory());
        video.setTags(request.getTags());
        video.setStatus(request.getStatus());

        // 3️⃣ Store Azure URLs (not local paths)
        video.setVideoUrl(videoBlobUrl);

        // Thumbnail placeholder (generate later)
        video.setThumbnailUrl(null);
        // 4️⃣ Store metadata
        video.setFileSize(file.getSize());
        video.setOriginalFilename(file.getOriginalFilename());

        VideoEntity savedVideo = videoRepository.save(video);

        // 5️⃣ Convert to response
        return convertToResponse(savedVideo);
    }


    private String getExtension(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName.substring(fileName.lastIndexOf("."));
    }
    public List<VideoResponseDto> getAllVideos() {
        return videoRepository.findAllByOrderByUploadedAtDesc()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<VideoResponseDto> getVideosByStatus(VideoStatus status) {
        return videoRepository.findByStatus(status.name())
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public VideoResponseDto getVideoById(String id) {
        VideoEntity video = findVideoById(id);
        return convertToResponse(video);
    }

    public VideoResponseDto updateVideo(String id, VideoUploadDto request) {
        VideoEntity video = findVideoById(id);

        video.setTitle(request.getTitle());
        video.setCaption(request.getCaption());
        video.setLocation(request.getLocation());
        video.setCategory(request.getCategory());
        video.setTags(request.getTags());
        video.setStatus(request.getStatus());

        VideoEntity updatedVideo = videoRepository.save(video);
        return convertToResponse(updatedVideo);
    }

    public void deleteVideo(String id) {
        VideoEntity video = findVideoById(id);

        // Delete file
        try {
            String filename = video.getVideoUrl().substring(video.getVideoUrl().lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir + filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        videoRepository.delete(video);
    }

    public void incrementViews(String id) {
        VideoEntity video = findVideoById(id);
        video.setViews(video.getViews() + 1);
        videoRepository.save(video);
    }

    public void incrementLikes(String id) {
        VideoEntity video = findVideoById(id);
        video.setLikes(video.getLikes() + 1);
        videoRepository.save(video);
    }

    private VideoResponseDto convertToResponse(VideoEntity video) {
        VideoResponseDto response = new VideoResponseDto();
        response.setId(video.getId());
        response.setTitle(video.getTitle());
        response.setCaption(video.getCaption());
        response.setLocation(video.getLocation());
        response.setVideoUrl(video.getVideoUrl());
        response.setThumbnailUrl(video.getThumbnailUrl());
        response.setCategory(video.getCategory());
        response.setTags(video.getTags());
        response.setStatus(video.getStatus());
        response.setViews(video.getViews());
        response.setLikes(video.getLikes());
        response.setFileSize(video.getFileSize());
        response.setOriginalFilename(video.getOriginalFilename());
        response.setUploadedAt(video.getUploadedAt()
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }

    public VideoEntity findVideoById(String id) {
        VideoEntity video = videoRepository.findById(id)
                .orElseThrow(() -> new AppBadException("Video not found"));
        return video;
    }

    public String generateStreamUrl(String id) {
        VideoEntity video = findVideoById(id);

        // Get the blob URL from the video entity
        String blobUrl = video.getVideoUrl();

        // Generate a SAS token for this blob
        String sasUrl = azureBlobService.generateReadSasUrl(blobUrl);

        return sasUrl;
    }
}
