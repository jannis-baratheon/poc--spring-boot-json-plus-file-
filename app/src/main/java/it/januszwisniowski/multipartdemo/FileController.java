package it.januszwisniowski.multipartdemo;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.util.Streams;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequestMapping("/api")
@RestController
public class FileController {
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // This is how to adjust OpenAPI specification to treat "metadata" as "application/json", unfortunately Swagger-UI does not recognize "encoding" part of the specification, so we have to use a @parameter workaround (https://github.com/springdoc/springdoc-openapi/issues/820#issuecomment-672875450)
    //    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(encoding = @Encoding(name = "metadata", contentType = "application/json")))
    public ResponseEntity<String> createFile(@RequestPart("metadata") @Parameter(schema = @Schema(type = "string", format = "binary")) FileMetadataDTO fileMetadataDTO,
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

    @GetMapping("/files")
    StreamingResponseBody getFile(@RequestParam("result") String expectedResult) {
        if (expectedResult.equals("ok")) {
            return outputStream -> {
                String alamakota = "costam  costam";

                IOUtils.copy(new ByteArrayInputStream(alamakota.getBytes(StandardCharsets.UTF_8)), outputStream);
            };
        }

        if (expectedResult.equals("notfound")) {
            return outputStream -> {
                throw new NotFoundException();
            };
        }

        throw new UnsupportedOperationException();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource not found!")
    @ExceptionHandler(NotFoundException.class)
    void handleNotFoundException() {
    }
}
