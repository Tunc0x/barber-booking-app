package com.example.SpringProject;


import com.example.SpringProject.clients.Appointment;
import com.example.SpringProject.clients.Barber;
import com.example.SpringProject.clients.Client;
import com.example.SpringProject.repository.AppointmentRepository;
import com.example.SpringProject.repository.BarberRepository;
import com.example.SpringProject.repository.ClientRepository;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class AppointmentControllerTest {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BarberRepository barberRepository;


    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String clientName = "Moritz Jung";
    private static Appointment testAppointment;

    @BeforeEach
    private void addAppointmentsToDb() {
        //Add Clients
        Client client = new Client();
        client.setName(clientName);
        client.setPhoneNumber("01793932102");
        client.setAge(19);
        client.setEmail("moritzjunger@gmail.com");
        client.setPassword("password123");
        clientRepository.save(client);

        //Add Barber
        Barber barber = new Barber();
        barber.setName("Okan");
        barberRepository.save(barber);

        //Add Appointment
        testAppointment = new Appointment();
        testAppointment.setClient(client);
        testAppointment.setBarber(barber);
        testAppointment.setDateTime(LocalDateTime.of(2023, 9, 23, 15, 45));
        appointmentRepository.save(testAppointment);


    }

    @AfterEach
    public void clearClientDb() {
        appointmentRepository.deleteAll();

        String sequenceName = "appointment_seq";
        jdbcTemplate.execute("ALTER SEQUENCE " + sequenceName + " RESTART WITH 1");
    }

    @Test
    void testGetRequest() {
        TestRestTemplate testRestTemplate = new TestRestTemplate();

        ResponseEntity<Appointment> appointmentEntity
                = testRestTemplate.getForEntity("http://localhost:8080/appointments/1", Appointment.class);
        Assertions.assertEquals(HttpStatus.OK, appointmentEntity.getStatusCode());
        Assertions.assertNotNull(appointmentEntity.getBody());
        Assertions.assertEquals(testAppointment, appointmentEntity.getBody());
    }

    @Test
    void testPostRequest()
    {
        //Add Client
        Client secondClient = new Client();
        secondClient.setName("Anna Bauer");
        secondClient.setAge(20);
        secondClient.setPhoneNumber("0178919387");
        secondClient.setEmail("annabauer@email.com");
        secondClient.setPassword("passwordAnna");
        clientRepository.save(secondClient);

        //Add Barber
        Barber barber = new Barber();
        barber.setName("Okan");
        barberRepository.save(barber);

        //Add Appointment
        testAppointment = new Appointment();
        testAppointment.setId(new Long(2));
        testAppointment.setClient(secondClient);
        testAppointment.setBarber(barber);
        testAppointment.setDateTime(LocalDateTime.of(2023, 10, 10, 15, 45));


        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = testRestTemplate.postForEntity("http://localhost:8080/appointments", testAppointment, String.class);
        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode() );

        ResponseEntity<Appointment> appointmentEntity
                = testRestTemplate.getForEntity("http://localhost:8080/appointments/2", Appointment.class);

        Assertions.assertNotNull(appointmentEntity.getBody());
        Assertions.assertEquals(testAppointment, appointmentEntity.getBody());


    }

    @Test
    void testAppointmentResponsesContainNoClientPassword()
    {
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        ResponseEntity<String> response = testRestTemplate.getForEntity("http://localhost:8080/appointments", String.class);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertTrue(response.getBody().contains(clientName));
        Assertions.assertFalse(response.getBody().contains("password"));
    }

}
