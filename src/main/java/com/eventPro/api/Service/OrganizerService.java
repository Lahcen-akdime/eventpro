package com.eventPro.api.Service;

import com.eventPro.api.Exception.InvalidVDataException;
import com.eventPro.api.Entity.Enum.Role;
import com.eventPro.api.Entity.Organizer;
import com.eventPro.api.Entity.User;
import com.eventPro.api.Repository.OrganisateurJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizerService {

    private static OrganisateurJpaRepository repository ;
    private final UserService userService ;
    private final PasswordEncoder passwordEncoder ;

    public User update(User user){
        return repository.save(user) ;
    }

    //public User save(CreateOrganizerDto dto){
    //    Optional<User> user = userService.findByEmail(email) ;
    //    if(user.isPresent()){
    //        throw new InvalidVDataException("L'email est deja appartient a un autre utilisateur ! ");
    //    }
//
    //    Organizer organizer = Organizer.builder()
    //            .name(name)
    //            .email(email)
    //            .password(passwordEncoder.encode(password))
    //            .role(Role.ORGANISER)
    //            .build() ;
//
    //    return userService.save(organizer);
    //}
}
