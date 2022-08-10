package it.januszwisniowski.multipartdemo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.springframework.web.reactive.function.BodyInserters.fromMultipartData;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class FileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private WebTestClient client;

    @BeforeEach
    final void setupTestClient() {
        client = MockMvcWebTestClient.bindTo(mockMvc).build();
    }

    @Test
    void createsFile() {
        String metadataJson = String.format("""
                        {
                            "name": "%s",
                            "tags": [%s]
                        }
                        """,
                "name of the file",
                Stream.of("tag1", "tag2", "tag3")
                        .map(s -> String.format("\"%s\"", s))
                        .collect(Collectors.joining(", ")));

        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("metadata", metadataJson, MediaType.APPLICATION_JSON);
        multipartBodyBuilder.part("file", "some file content").filename("alamakota");

        client.post()
                .uri("/api/files")
                .body(fromMultipartData(multipartBodyBuilder.build()))
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody()
                .consumeWith(bodyContent ->
                        assertArrayEquals("""
                                        name of the file
                                        [tag1, tag2, tag3]
                                        some file content
                                        """.getBytes(StandardCharsets.UTF_8),
                                bodyContent.getResponseBody()));
    }

    @Test
    void getsFile() {
        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/files")
                        .queryParam("result", "ok")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(bodyContent ->
                        assertArrayEquals(
                                "costam  costam".getBytes(StandardCharsets.UTF_8),
                                bodyContent.getResponseBody()));
    }

    @Test
    void serverErrorWhenErrorInStreamingResponseBodyOccurs() {
        client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/files")
                        .queryParam("result", "notfound")
                        .build())
                .exchange()
                .expectStatus()
                .isNotFound();
    }
}
