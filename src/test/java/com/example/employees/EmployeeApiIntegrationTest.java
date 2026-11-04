package com.example.employees;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsReadsAndUpdatesEmployeesUsingFallback() throws Exception {
        String employee = """
                {"id":41,"name":"Riley Chen","department":"Engineering","salary":92000.5,"age":32}
                """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employee))
                .andExpect(status().isCreated())
                .andExpect(header().string("X-Data-Source", "local-fallback"))
                .andExpect(jsonPath("$.id", is(41)));

        mockMvc.perform(get("/api/employees").param("department", "Engineering"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Data-Source", "local-fallback"))
                .andExpect(jsonPath("$[0].name", is("Riley Chen")));

        String updatedEmployee = """
                {"id":41,"name":"Riley Chen","department":"Platform","salary":98000.0,"age":33}
                """;
        mockMvc.perform(put("/api/employees/41")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedEmployee))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Data-Source", "local-fallback"))
                .andExpect(jsonPath("$.department", is("Platform")));
    }

    @Test
    void reportsInvalidEmployeeAsSchemaError() throws Exception {
        String invalidEmployee = """
                {"id":0,"name":"","department":"Engineering","salary":-1,"age":32}
                """;

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEmployee))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("SCHEMA_ERROR")));
    }

    @Test
    void reportsMismatchedPathAndBodyIdsAsBusinessError() throws Exception {
        String employee = """
                {"id":42,"name":"Riley Chen","department":"Engineering","salary":92000.5,"age":32}
                """;

        mockMvc.perform(put("/api/employees/41")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(employee))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code", is("BUSINESS_ERROR")));
    }
}
