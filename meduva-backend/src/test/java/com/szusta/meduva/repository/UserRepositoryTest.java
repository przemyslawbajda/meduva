package com.szusta.meduva.repository;

import com.szusta.meduva.model.ERole;
import com.szusta.meduva.model.Role;
import com.szusta.meduva.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepositoryUnderTest;
    @Autowired
    private RoleRepository roleRepository;

    @Test
    void findAllClientsWithAccount() {
        // given
        User userClient = new User(
                "login",
                "email",
                "password",
                "name",
                "surname",
                "phoneNumber"
        );
        userClient.setRoles(Set.of(roleRepository.getById(ERole.ROLE_CLIENT.getValue())));
        userClient = userRepositoryUnderTest.save(userClient);

        User userWorker = new User(
                "login2",
                "email2",
                "password2",
                "name2",
                "surname2",
                "phoneNumber2"
        );
        userWorker.setRoles(Set.of(
                roleRepository.getById(ERole.ROLE_CLIENT.getValue()),
                roleRepository.getById(ERole.ROLE_WORKER.getValue())));
        userWorker = userRepositoryUnderTest.save(userWorker);

        // when
        Optional<List<User>> clients =
                userRepositoryUnderTest.findAllClientsWithAccount();

        // then
        assertTrue(clients.isPresent());
        assertEquals(1, clients.get().size());
        assertEquals(userClient.getName(), clients.get().get(0).getName());
    }
}