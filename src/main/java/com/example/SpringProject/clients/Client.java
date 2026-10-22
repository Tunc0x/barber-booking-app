package com.example.SpringProject.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_seq_gen")
    @SequenceGenerator(name = "client_seq_gen", sequenceName = "client_seq", allocationSize = 1, initialValue = 1)
    private Long id;
    @NotBlank(message = "Name may not be blank")
    private String name;

    @NotBlank(message = "Phone number may not be blank")
    private String phoneNumber;

    @Min(value = 5, message = "Age should not be less than 5")
    @Positive(message = "Age should be positive")
    private int age;

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email Address may not be blank")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    //  private LocalDateTime appointmentDateTime;


    //  private String barber;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }




    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object obj) {
        Client testClient = (Client)obj;

        return testClient.getId().equals(this.getId());




    }









}
