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
import ru.gerilovich.dto.OwnerDto;
import ru.gerilovich.models.Role;
import ru.gerilovich.services.OwnerService;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/owners")
public class OwnerController {

    private final OwnerService ownerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("permitAll()")
    public Long createOwner(@RequestBody OwnerDto ownerDto) {
        ownerDto.setRole(Role.USER);
        OwnerDto createdOwner = ownerService.save(ownerDto);
        return createdOwner.getId();
    }

    @GetMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public OwnerDto getOwnerById(@PathVariable(name = "id") @Positive(message = "ID must be a positive number") @P("id") Long id) {
        return ownerService.getById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public OwnerDto updateOwner(@PathVariable(name = "id") @Positive(message = "ID must be a positive number") @P("id") Long id, @RequestBody OwnerDto ownerDto) {
        return ownerService.update(id, ownerDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    public void deleteOwnerById(@PathVariable(name = "id") @Positive(message = "ID must be a positive number") @P("id") Long id) {
        ownerService.deleteById(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("#ownerDto.id == authentication.principal.id or hasRole('ADMIN')")
    public void deleteOwner(@RequestBody @P("ownerDto") OwnerDto ownerDto) {
        ownerService.deleteByEntity(ownerDto);
    }

    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAllOwners() {
        ownerService.deleteAll();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<OwnerDto> listOwners() {
        return ownerService.getAll();
    }

    @GetMapping("/filter")
    @PreAuthorize("isAuthenticated()")
    public Page<OwnerDto> getOwnersWithFilter(
            @RequestParam(name = "page", defaultValue = "0") @Min(value = 0, message = "Page number must be positive") int page,
            @RequestParam(name = "size", defaultValue = "5") @Min(value = 1, message = "Page size must be at least 1") int size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "birthdate", required = false) LocalDate birthDate,
            @RequestParam(value = "petIds", required = false) List<@Positive(message = "ID must be a positive number") Long> petIds) {

        return ownerService.getOwnersWithFilter(page, size, name, birthDate, petIds);

    }
}
