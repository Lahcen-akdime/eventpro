package com.eventPro.api.Entity;


import com.eventPro.api.Entity.Enum.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@NoArgsConstructor
@SuperBuilder
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id ;

    @NotBlank
    @Column(nullable = false,length = 100)
    protected String name ;

    @Email
    @NotBlank
    @Column(unique = true,nullable = false)
    protected String email ;

    @Column(nullable = false,length = 100)
    protected String password ;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected Role role ;

}