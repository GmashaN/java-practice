package ru.gerilovich.mappers;

import lombok.NonNull;
import ru.gerilovich.dto.PetDto;
import ru.gerilovich.models.Owner;
import ru.gerilovich.models.Pet;

import java.util.List;

public class PetMapper {
    public PetDto toDto(@NonNull Pet pet) {
        PetDto petDto = new PetDto();
        petDto.setId(pet.getId());
        petDto.setName(pet.getName());
        petDto.setBirthDate(pet.getBirthDate());
        petDto.setBreed(pet.getBreed());
        petDto.setColor(pet.getColor());
        if (pet.getOwner() != null) {
            petDto.setOwner(pet.getOwner().getId());
        }
        if (!pet.getFriends().isEmpty()) {
            pet.getFriends().forEach(friend -> petDto.getFriendIds().add(friend.getId()));
        }
        return petDto;
    }

    public Pet toEntity(PetDto petDto, List<Pet> pets, Owner owner) {
        if (petDto == null) {
            return null;
        }
        Pet pet = new Pet();
        pet.setId(petDto.getId());
        pet.setName(petDto.getName());
        pet.setBirthDate(petDto.getBirthDate());
        pet.setBreed(petDto.getBreed());
        pet.setColor(petDto.getColor());
        pet.setOwner(owner);
        pet.setFriends(pets);
        return pet;
    }

}
