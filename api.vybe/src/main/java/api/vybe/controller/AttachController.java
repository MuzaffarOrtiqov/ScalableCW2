package api.vybe.controller;


import api.vybe.dto.attach.AttachDTO;
import api.vybe.service.AttachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/attach")
@Tag(name = "AttachController", description = "A set of APIs to work with attach")
@Slf4j
public class AttachController {
    @Autowired
    private AttachService attachService;


    @PostMapping("/upload")
    @Operation(summary = "Upload multipart file",description ="Method used to upload any sort of file" )
    public ResponseEntity<AttachDTO> create(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(attachService.upload(file));
    }

    @GetMapping("/open/{fileName}")
    @Operation(summary = "Open multipart file",description ="Method used to open any type of file" )
    public ResponseEntity<Resource> open(@PathVariable String fileName) {
        return attachService.open(fileName);
    }
}
