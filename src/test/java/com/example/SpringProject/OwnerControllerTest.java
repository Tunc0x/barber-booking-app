package com.example.SpringProject;

import com.example.SpringProject.clients.LoginRequest;
import com.example.SpringProject.repository.OwnerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class OwnerControllerTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    public void clearOwnerDb() {
        ownerRepository.deleteAll();
    }

    @Test
    void testOwnerPasswordIsStoredHashed() {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = testRestTemplate.postForEntity("http://localhost:8080/owner", newOwner("owner@shop.com", "ownerSecret"), String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNull(response.getBody());

        String storedPassword = ownerRepository.findOwnerByEmail("owner@shop.com").orElseThrow().getPassword();
        Assertions.assertNotEquals("ownerSecret", storedPassword);
        Assertions.assertTrue(passwordEncoder.matches("ownerSecret", storedPassword));
    }

    @Test
    void testOwnerLoginWithCorrectAndWrongPassword() {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        testRestTemplate.postForEntity("http://localhost:8080/owner", newOwner("owner@shop.com", "ownerSecret"), String.class);

        Assertions.assertEquals(Boolean.TRUE, testRestTemplate.postForObject("http://localhost:8080/owner/login", loginRequest("owner@shop.com", "ownerSecret"), Boolean.class));
        Assertions.assertEquals(Boolean.FALSE, testRestTemplate.postForObject("http://localhost:8080/owner/login", loginRequest("owner@shop.com", "wrong"), Boolean.class));
    }

    // Plain JSON body: the password is write-only on Owner, so serializing the entity would drop it.
    private static Map<String, Object> newOwner(String email, String password) {
        return Map.of("email", email, "password", password);
    }

    private static LoginRequest loginRequest(String email, String password) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        return loginRequest;
    }
}
