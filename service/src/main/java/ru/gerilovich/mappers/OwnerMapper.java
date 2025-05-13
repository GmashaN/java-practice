package ru.gerilovich.mappers;

import lombok.NonNull;
import ru.gerilovich.dto.OwnerDto;
import ru.gerilovich.models.Owner;
import ru.gerilovich.models.Pet;

import java.util.List;

public class OwnerMapper {
    public OwnerDto toDto(@NonNull Owner owner) {
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setId(owner.getId());
        ownerDto.setName(owner.getName());
        ownerDto.setBirthDate(owner.getBirthDate());
        owner.getPets().forEach(pet -> ownerDto.getPetIds().add(pet.getId()));
        ownerDto.setPassword(owner.getPassword());
        ownerDto.setRole(owner.getRole());
        return ownerDto;
    }

    public Owner toEntity(OwnerDto ownerDto, List<Pet> pets) {
        if (ownerDto == null) {
            return null;
        }
        Owner owner = new Owner();
        if (ownerDto.getId() != null) {
            owner.setId(ownerDto.getId());
        }
        owner.setName(ownerDto.getName());
        owner.setBirthDate(ownerDto.getBirthDate());
        owner.setPets(pets);
        owner.getPets().forEach(pet -> pet.setOwner(owner));
        owner.setPassword(ownerDto.getPassword());
        owner.setRole(ownerDto.getRole());
        return owner;
    }
}
