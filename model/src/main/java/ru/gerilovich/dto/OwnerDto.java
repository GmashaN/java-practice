package ru.gerilovich.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.gerilovich.models.Role;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OwnerDto {
    private Long id;
    private String name;
    private LocalDate birthDate;
    private List<Long> petIds = new ArrayList<>();
    private Role role;
    private String password;

    public OwnerDto(String ownerName, LocalDate birthDate, List<Long> petIds, Role role, String password) {
        this.name = ownerName;
        this.birthDate = birthDate;
        this.petIds = petIds;
        this.role = role;
        this.password = password;
    }
}
