package com.eventPro.api.Model;


import com.eventPro.api.Model.Enum.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
abstract public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    protected UUID id ;

    @NotNull
    @NotBlank
    @NotEmpty
    protected String name ;

    @Email
    @Column(unique = true)
    protected String email ;

    protected Role role ;

    public User(String name , String email){
        this.name = name ;
        this.email = email ;
    }

    public User() {

    }
}