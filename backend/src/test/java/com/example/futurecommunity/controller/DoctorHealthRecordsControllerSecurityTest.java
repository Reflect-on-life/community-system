package com.example.futurecommunity.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security tests for DoctorHealthRecordsController endpoints.
 * Note: Requires test DB state or SQL setup to reflect resident associations.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class DoctorHealthRecordsControllerSecurityTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  @Disabled("Requires test DB setup: resident association for userId=1 and proper DOCTOR role")
  @WithMockUser(roles = "DOCTOR", username = "1")
  void doctorCanAccessHealthRecords() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/doctor-residents/health-records/1"))
      .andExpect(status().isOk());
  }

  @Test
  @Disabled("Pending DB setup for health-check creation under DOCTOR role with owned resident")
  @WithMockUser(roles = "DOCTOR", username = "1")
  void doctorCanAccessOwnResidentHealthCheck() throws Exception {
    String payload = "{\"userId\":1,\"height\":170,\"weight\":65,\"bloodPressure\":\"110/70\",\"bloodSugar\":5.5,\"chronicDisease\":\"\",\"doctorSuggestion\":\"OK\",\"notes\":\"test\"}";
    mockMvc.perform(post("/api/doctor-residents/health-check")
            .contentType(MediaType.APPLICATION_JSON)
            .content(payload))
      .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "RESIDENT", username = "2")
  void nonDoctorCannotAccessHealthRecords() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/api/doctor-residents/health-records/1"))
      .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "DOCTOR", username = "1")
  void doctorDeniedIfResidentNotAssigned() throws Exception {
    mockMvc.perform(get("/api/doctor-residents/health-records/-1"))
      .andExpect(status().isForbidden());
  }

  @Test
  void unauthorizedAccessShouldBeUnauthorized() throws Exception {
    mockMvc.perform(get("/api/doctor-residents/health-records/1"))
      .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "DOCTOR", username = "1")
  void doctorCanAccessHealthStats() throws Exception {
    mockMvc.perform(get("/api/doctor-residents/health-stats"))
      .andExpect(status().isOk());
  }
}
