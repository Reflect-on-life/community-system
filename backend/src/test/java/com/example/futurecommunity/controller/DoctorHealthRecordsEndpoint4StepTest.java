package com.example.futurecommunity.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 4th step integration tests for DoctorHealthRecords endpoints.
 * These tests are designed to run in a properly configured test environment.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class DoctorHealthRecordsEndpoint4StepTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @WithMockUser(roles = "DOCTOR", username = "1")
  void doctorCanAccessHealthRecordsList() throws Exception {
    mockMvc.perform(get("/api/doctor-residents/list"))
      .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "RESIDENT", username = "2")
  void residentCannotAccessHealthRecordsList() throws Exception {
    mockMvc.perform(get("/api/doctor-residents/list"))
      .andExpect(status().isForbidden());
  }

  @Test
  @Disabled("Requires DB setup for health-check create under DOCTOR and owned resident")
  @WithMockUser(roles = "DOCTOR", username = "1")
  void doctorCanCreateHealthCheckForOwnedResident() throws Exception {
    String payload = "{\"userId\":1,\"height\":170,\"weight\":65,\"bloodPressure\":\"110/70\",\"bloodSugar\":5.5,\"chronicDisease\":\"\",\"doctorSuggestion\":\"OK\",\"notes\":\"test\"}";
    mockMvc.perform(post("/api/doctor-residents/health-check")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
      .andExpect(status().isOk());
  }
}
