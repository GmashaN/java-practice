package ru.gerilovich.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.gerilovich.models.Color;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PetDto {
    private Long id;
    private String name;
    private LocalDate birthDate;
    private String breed;
    private Color color;
    private Long owner;
    private List<Long> friendIds = new ArrayList<>();

    public PetDto(String name, LocalDate birthDate, String breed, Color color, Long owner, List<Long> friendIds) {
        this.name = name;
        this.birthDate = birthDate;
        this.breed = breed;
        this.color = color;
        this.owner = owner;
        this.friendIds = friendIds;
    }
}
