package com.example.SpringProject;


import com.example.SpringProject.clients.Client;
import com.example.SpringProject.clients.LoginRequest;
import com.example.SpringProject.repository.ClientRepository;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class ClientControllerTest {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String clientName = "Moritz Jung";

    @BeforeEach
    private void addClientsToDb() {
        Client client = new Client();
        client.setName(clientName);
        client.setPhoneNumber("01793932102");
        client.setAge(19);
        client.setEmail("moritzjunger@gmail.com");
        client.setPassword("password123");

        clientRepository.save(client);

    }

    @AfterEach
    public void clearClientDb() {
        clientRepository.deleteAll();

        String sequenceName = "client_seq";
        jdbcTemplate.execute("ALTER SEQUENCE " + sequenceName + " RESTART WITH 1");
    }

    @Test
    void testGetRequest() {
        TestRestTemplate testRestTemplate = new TestRestTemplate();

        ResponseEntity<Client> clientEntity
                = testRestTemplate.getForEntity("http://localhost:8080/clients/1", Client.class);
        Assertions.assertEquals(HttpStatus.OK, clientEntity.getStatusCode());
        Assertions.assertNotNull(clientEntity.getBody());
        Assertions.assertEquals(clientName, clientEntity.getBody().getName());
    }

    @Test
    void testPostRequest()
    {
        Client secondClient = new Client();
        secondClient.setName("Anna Bauer");
        secondClient.setAge(20);
        secondClient.setPhoneNumber("0178919387");
        secondClient.setEmail("annabauer@email.com");
        secondClient.setPassword("passwordAnna");


        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = testRestTemplate.postForEntity("http://localhost:8080/clients", secondClient, String.class);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode() );

        ResponseEntity<Client> clientEntity
                = testRestTemplate.getForEntity("http://localhost:8080/clients/2", Client.class);

        Assertions.assertNotNull(clientEntity.getBody());
        Assertions.assertEquals("Anna Bauer", clientEntity.getBody().getName());
        Assertions.assertEquals(20, clientEntity.getBody().getAge());
        Assertions.assertEquals("0178919387", clientEntity.getBody().getPhoneNumber());
        Assertions.assertEquals("annabauer@email.com", clientEntity.getBody().getEmail());
        Assertions.assertNull(clientEntity.getBody().getPassword());

    }

    @Test
    void testDeleteRequest() {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        testRestTemplate.delete("http://localhost:8080/clients/1", Client.class);

        ResponseEntity<Client> clientEntity
                = testRestTemplate.getForEntity("http://localhost:8080/clients/1", Client.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, clientEntity.getStatusCode());

    }

    @Test
    void testInvalidPostRequest()
    {
        // Invalid Name
        Client secondClient = new Client();
        secondClient.setName("");
        secondClient.setAge(10);
        secondClient.setPhoneNumber("01793932102");
        secondClient.setEmail("hans@gmail.com");


        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<Client> response = testRestTemplate.postForEntity("http://localhost:8080/clients", secondClient, Client.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode() );

        // Invalid Age
        Client thirdClient = new Client();
        thirdClient.setName("John Cube");
        thirdClient.setAge(3);
        thirdClient.setPhoneNumber("01793932102");
        thirdClient.setEmail("johncube@email.com");


        response = testRestTemplate.postForEntity("http://localhost:8080/clients", thirdClient, Client.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode() );

        // Invalid phone number
        Client fourthClient = new Client();
        fourthClient.setName("Anna Cube");
        fourthClient.setAge(10);
        fourthClient.setPhoneNumber("");
        fourthClient.setEmail("annacube@email.com");


        response = testRestTemplate.postForEntity("http://localhost:8080/clients", fourthClient, Client.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode() );

    }




    @Test
    void testPasswordIsStoredHashed()
    {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = testRestTemplate.postForEntity("http://localhost:8080/clients", newClient("Lena Hoff", "lena@email.com", "secret123"), String.class);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());

        String storedPassword = clientRepository.findClientByEmail("lena@email.com").orElseThrow().getPassword();
        Assertions.assertNotEquals("secret123", storedPassword);
        Assertions.assertTrue(storedPassword.startsWith("$2"));
        Assertions.assertTrue(passwordEncoder.matches("secret123", storedPassword));
    }

    @Test
    void testLoginWithCorrectAndWrongPassword()
    {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        testRestTemplate.postForEntity("http://localhost:8080/clients", newClient("Lena Hoff", "lena@email.com", "secret123"), String.class);

        Assertions.assertEquals(Boolean.TRUE, testRestTemplate.postForObject("http://localhost:8080/clients/loginValid", loginRequest("lena@email.com", "secret123"), Boolean.class));
        Assertions.assertEquals(Boolean.FALSE, testRestTemplate.postForObject("http://localhost:8080/clients/loginValid", loginRequest("lena@email.com", "wrong"), Boolean.class));

        ResponseEntity<String> login = testRestTemplate.postForEntity("http://localhost:8080/clients/login", loginRequest("lena@email.com", "secret123"), String.class);
        Assertions.assertEquals(HttpStatus.OK, login.getStatusCode());
        Assertions.assertTrue(login.getBody().contains("lena@email.com"));
        Assertions.assertFalse(login.getBody().contains("password"));

        ResponseEntity<String> failedLogin = testRestTemplate.postForEntity("http://localhost:8080/clients/login", loginRequest("lena@email.com", "wrong"), String.class);
        Assertions.assertNull(failedLogin.getBody());
    }

    @Test
    void testResponsesContainNoPassword()
    {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> created = testRestTemplate.postForEntity("http://localhost:8080/clients", newClient("Lena Hoff", "lena@email.com", "secret123"), String.class);
        Assertions.assertFalse(created.getBody().contains("password"));

        ResponseEntity<String> one = testRestTemplate.getForEntity("http://localhost:8080/clients/1", String.class);
        Assertions.assertEquals(HttpStatus.OK, one.getStatusCode());
        Assertions.assertFalse(one.getBody().contains("password"));

        ResponseEntity<String> all = testRestTemplate.getForEntity("http://localhost:8080/clients", String.class);
        Assertions.assertTrue(all.getBody().contains("lena@email.com"));
        Assertions.assertFalse(all.getBody().contains("password"));
    }

    // Plain JSON body: the password is write-only on Client, so serializing the entity would drop it.
    private static Map<String, Object> newClient(String name, String email, String password)
    {
        return Map.of("name", name, "age", 25, "phoneNumber", "0170123456", "email", email, "password", password);
    }

    private static LoginRequest loginRequest(String email, String password)
    {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(password);
        return loginRequest;
    }

}
