package it.januszwisniowski.multipartdemo;

import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequestMapping("/api/files")
@RestController
public class FileController {
    @PostMapping
    public ResponseEntity<String> createFile(@RequestPart("metadata") FileMetadataDTO fileMetadataDTO,
                                             @RequestPart("file") MultipartFile file) throws URISyntaxException, IOException {
        try (InputStream is = file.getInputStream()) {
            return ResponseEntity
                    .created(new URI("/api/files/" + UUID.randomUUID()))
                    .body(Stream.of(fileMetadataDTO.name(), fileMetadataDTO.tags(), Streams.asString(is))
                            .map(Object::toString)
                            .collect(Collectors.joining("\n"))
                            .concat("\n"));
        }
    }
}
