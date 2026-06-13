package com.example.futurecommunity.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * End-to-end health records flow for doctors.
 * Requires preloaded test data via SQL script.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Sql({"classpath:test-health-end2end.sql"})
public class DoctorHealthRecordsEndToEndTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @WithMockUser(roles = "DOCTOR", username = "1")
  void endToEndHealthCheckFlow() throws Exception {
    String payload = "{\"userId\":1,\"height\":170,\"weight\":65,\"bloodPressure\":\"110/70\",\"bloodSugar\":5.6,\"chronicDisease\":\"\",\"doctorSuggestion\":\"OK\",\"notes\":\"end-to-end test\"}";
    // 1) create health check
    mockMvc.perform(post("/api/doctor-residents/health-check")
      .contentType(MediaType.APPLICATION_JSON)
      .content(payload))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data.id").exists());

    // 2) fetch health records for user
    mockMvc.perform(get("/api/doctor-residents/health-records/1"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.data").exists());
  }
}
