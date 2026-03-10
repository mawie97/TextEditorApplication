package com.example.texteditorapi.editor.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class DocumentControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createDocument_returnsCreatedDocument() throws Exception {
        String requestJson = """
                {
                  "title": "My note",
                  "text": "hello"
                }
                """;

        mockMvc.perform(
                        post("/documents")
                                .contentType(APPLICATION_JSON)
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("My note"))
                .andExpect(jsonPath("$.text").value("hello"))
                .andExpect(jsonPath("$.cursor").value(5))
                .andExpect(jsonPath("$.anchor").value(5))
                .andExpect(jsonPath("$.preferredColumn").value(5))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void getDocumentById_returnsDocument() throws Exception {
        String createRequest = """
                {
                  "title": "Fetch me",
                  "text": "hello"
                }
                """;

        String createResponse = mockMvc.perform(
                        post("/documents")
                                .contentType(APPLICATION_JSON)
                                .content(createRequest)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = extractJsonString(createResponse, "id");

        mockMvc.perform(get("/documents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Fetch me"))
                .andExpect(jsonPath("$.text").value("hello"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void listDocuments_returnsSummaries() throws Exception {
        mockMvc.perform(
                        post("/documents")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "Doc A",
                                          "text": "aaa"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/documents")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "Doc B",
                                          "text": "bbb"
                                        }
                                        """)
                )
                .andExpect(status().isOk());

        mockMvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].updatedAt").exists());
    }

    @Test
    void applyInsertCommand_updatesDocument() throws Exception {
        String createResponse = mockMvc.perform(
                        post("/documents")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "Command doc",
                                          "text": "hello"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = extractJsonString(createResponse, "id");
        String createdAt = extractJsonString(createResponse, "createdAt");

        mockMvc.perform(
                        post("/documents/{id}/commands", id)
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "type": "INSERT",
                                          "text": " world"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.title").value("Command doc"))
                .andExpect(jsonPath("$.text").value("hello world"))
                .andExpect(jsonPath("$.cursor").value(11))
                .andExpect(jsonPath("$.anchor").value(11))
                .andExpect(jsonPath("$.preferredColumn").value(11))
                .andExpect(jsonPath("$.createdAt").value(createdAt))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void getUnknownDocument_returns404() throws Exception {
        String unknownId = "11111111-1111-1111-1111-111111111111";

        mockMvc.perform(get("/documents/{id}", unknownId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/documents/" + unknownId));
    }

    @Test
    void invalidCommandRequest_returns400() throws Exception {
        String createResponse = mockMvc.perform(
                        post("/documents")
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "title": "Bad command doc",
                                          "text": "hello"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String id = extractJsonString(createResponse, "id");

        mockMvc.perform(
                        post("/documents/{id}/commands", id)
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "pos": -1
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/documents/" + id + "/commands"));
    }

    private static String extractJsonString(String json, String fieldName) {
        String needle = "\"" + fieldName + "\":\"";
        int start = json.indexOf(needle);
        if (start < 0) {
            throw new IllegalArgumentException("Field not found: " + fieldName);
        }
        start += needle.length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            throw new IllegalArgumentException("Could not parse field: " + fieldName);
        }
        return json.substring(start, end);
    }
}