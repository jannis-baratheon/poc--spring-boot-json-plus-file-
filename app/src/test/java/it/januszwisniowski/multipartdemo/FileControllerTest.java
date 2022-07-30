package it.januszwisniowski.multipartdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class FileControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsFile() throws Exception {
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

        MockMultipartHttpServletRequestBuilder multipart =
                MockMvcRequestBuilders.multipart("/api/files")
                        .file(new MockMultipartFile(
                                "metadata",
                                null,
                                MediaType.APPLICATION_JSON_VALUE,
                                metadataJson.getBytes(StandardCharsets.UTF_8)))
                        .file("file", "some file content".getBytes(StandardCharsets.UTF_8));

        mockMvc
                .perform(multipart)
                .andExpect(status().isCreated())
                .andExpect(content().string("""
                        name of the file
                        [tag1, tag2, tag3]
                        some file content
                        """));
    }
}
