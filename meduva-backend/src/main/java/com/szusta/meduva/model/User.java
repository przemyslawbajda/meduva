package com.szusta.meduva.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.szusta.meduva.model.common.Undeletable;
import com.szusta.meduva.model.role.Role;
import com.szusta.meduva.model.schedule.visit.Visit;
import com.szusta.meduva.model.schedule.WorkerSchedule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "user",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "login"),
                @UniqueConstraint(columnNames = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends Undeletable {

    private String login;
    private String email;
    private String password;

    private String name;
    private String surname;
    @Column(name = "phone_number")
    private String phoneNumber;

    @ManyToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    List<Visit> visits;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<WorkerSchedule> workerSchedules;

    public User(String login, String email, String password) {
        this.login = login;
        this.email = email;
        this.password = password;
    }

    public User(String login, String email, String password, String name, String surname, String phoneNumber) {
        this.login = login;
        this.email = email;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
    }
}
