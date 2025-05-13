package ru.gerilovich.controllers;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.gerilovich.dto.PetDto;
import ru.gerilovich.models.Color;
import ru.gerilovich.services.PetService;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/pets")
public class PetController {
    private final PetService petService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("#petDto.owner == authentication.principal.id or hasRole('ADMIN')")
    public Long createPet(@RequestBody @P("petDto") PetDto petDto) {
        return petService.save(petDto).getId();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PetDto getPetById(@PathVariable(name = "id") @P("id") Long id) {
        return petService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@petService.getIsPetOwner(#id, authentication.principal.id) or hasRole('ADMIN')")
    public PetDto updatePet(@PathVariable(name = "id") @Positive(message = "ID must be a positive number") @P("id") Long id, @RequestBody PetDto petDto) {
        return petService.update(id, petDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@petService.getIsPetOwner(#id, authentication.principal.id) or hasRole('ADMIN')")
    public void deletePetById(@PathVariable(name = "id") @Positive(message = "ID must be a positive number") @P("id") Long id) {
        petService.deleteById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@petService.getIsPetOwner(#petDto.id, authentication.principal.id) or hasRole('ADMIN')")
    public void deletePet(@RequestBody @P("petDto") PetDto petDto) {
        petService.deleteByEntity(petDto);
    }

    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAllPets() {
        petService.deleteAll();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<PetDto> listPets() {
        return petService.getAll();
    }

    @GetMapping("/filter")
    @PreAuthorize("isAuthenticated()")
    public Page<PetDto> getPetsWithFilter(
            @RequestParam(name = "page", defaultValue = "0") @Min(value = 0, message = "Page number must be positive") int page,
            @RequestParam(name = "size", defaultValue = "5") @Min(value = 1, message = "Page size must be at least 1") int size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "birthdate", required = false) LocalDate birthDate,
            @RequestParam(value = "breed", required = false) String breed,
            @RequestParam(value = "color", required = false) Color color,
            @RequestParam(value = "ownerId", required = false) @Positive(message = "ID must be a positive number") Long ownerId,
            @RequestParam(value = "friendIds", required = false) List<@Positive(message = "ID must be a positive number") Long> friendIds) {

        return petService.getPetsWithFilter(page, size, name, birthDate, breed, color, ownerId, friendIds);

    }
}
