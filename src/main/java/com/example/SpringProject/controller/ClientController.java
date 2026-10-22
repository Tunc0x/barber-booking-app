package com.example.SpringProject.controller;

import com.example.SpringProject.clients.Client;
import com.example.SpringProject.clients.LoginRequest;
import com.example.SpringProject.clients.Owner;
import com.example.SpringProject.error.EntityNotFoundException;
import com.example.SpringProject.error.DuplicateEntryException;
import com.example.SpringProject.repository.ClientRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/clients")
@CrossOrigin
public class ClientController {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired // Spring weiß, dass das eine Klasse (Bean) existiert im ApplicationContext und fügt diese automatisch ein.
    // Inversion of Control
    public ClientController(ClientRepository clientRepository, PasswordEncoder passwordEncoder)
    {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("") // RequestBody -> Informationen im Body werden gelesen und wird in das Java Object Client konvertiert
    public ResponseEntity<Map<String, Object>> createClient(@Valid @RequestBody Client client) throws DuplicateEntryException {
        Optional<Client> refClient = clientRepository.findClientByEmail(client.getEmail());

        if(refClient.isPresent())
        {
            throw new DuplicateEntryException("Email already exists.");
        } else {
            // Guests book without a password; only real passwords are hashed and stored.
            String rawPassword = client.getPassword();
            client.setPassword(rawPassword == null || rawPassword.isEmpty() ? null : passwordEncoder.encode(rawPassword));
            Client savedClient =  clientRepository.save(client);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "The appointment was booked for the client.");
            response.put("clientId", savedClient.getId());

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }


    }

    @GetMapping("/{clientId}")     // Mit PathVariable sagen wir Spring, dass es in unserem Pfad nachschauen soll.
    public Client readClient(@PathVariable Long clientId) throws EntityNotFoundException {
        Optional<Client> client = clientRepository.findById(clientId);
        if(client.isPresent())
        {
            return client.get();
        }
        throw new EntityNotFoundException("Client with this id not found");
    }

    @GetMapping()
    public Iterable<Client> readAllClients()
    {
        return clientRepository.findAll();
    }

 /*   @GetMapping("/appointments/{appointmentDateTime}")     // Mit PathVariable sagen wir Spring, dass es in unserem Pfad nachschauen soll.
    public Client readClient(@PathVariable LocalDateTime clientAppointmentDateTime)
    {
        Optional<Client> client = clientRepository.findClientByAppointmentDateTime(clientAppointmentDateTime);
        if(client.isPresent())
        {
            return client.get();
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No date is set for this time");
    } */



    @PutMapping("/{clientId}")
    public void updateClientById(@Valid @RequestBody Client clientUpdate, @PathVariable Long clientId) throws EntityNotFoundException {
        Optional<Client> client = clientRepository.findById(clientId);
        if(!client.isPresent())
        {
            throw new EntityNotFoundException("Client with this id not found");

        }
        Client clientInstance = client.get();
        clientInstance.setName(clientUpdate.getName());
        clientInstance.setAge(clientUpdate.getAge());
        clientInstance.setPhoneNumber(clientUpdate.getPhoneNumber());
        clientInstance.setEmail(clientUpdate.getEmail());


        clientRepository.save(clientInstance);


    }

    @DeleteMapping("/{clientId}")
    public void deleteClient( @PathVariable Long clientId) throws EntityNotFoundException {
        Optional<Client> client = clientRepository.findById(clientId);
        if(client.isPresent())
        {
            clientRepository.deleteById(clientId);
            return;
        }
        throw new EntityNotFoundException("Client with this id not found");

    }


    @PostMapping("/loginValid")
    public boolean isClientLoginValid(@RequestBody LoginRequest loginRequest)
    {
        Optional<Client> optionalClient = clientRepository.findClientByEmail(loginRequest.getEmail());
        if(optionalClient.isEmpty())
        {
            return false;
        }


        Client client = optionalClient.get();
        if(passwordMatches(loginRequest.getPassword(), client.getPassword()))
        {
            return true;
        }

        return false;

    }



    @PostMapping("/login")
    public Client ClientLogin(@RequestBody LoginRequest loginRequest)
    {
        Optional<Client> optionalClient = clientRepository.findClientByEmail(loginRequest.getEmail());
        if(optionalClient.isEmpty())
        {
            return null;
        }


        Client client = optionalClient.get();
        if(passwordMatches(loginRequest.getPassword(), client.getPassword()))
        {
            return client;
        }

        return null;

    }


    private boolean passwordMatches(String rawPassword, String passwordHash)
    {
        return rawPassword != null && passwordHash != null && passwordEncoder.matches(rawPassword, passwordHash);
    }

}
