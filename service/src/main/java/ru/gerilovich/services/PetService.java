package ru.gerilovich.services;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gerilovich.dao.OwnerDao;
import ru.gerilovich.dao.PetDao;
import ru.gerilovich.dto.PetDto;
import ru.gerilovich.mappers.PetMapper;
import ru.gerilovich.models.Color;
import ru.gerilovich.models.Owner;
import ru.gerilovich.models.Pet;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service("petService")
@RequiredArgsConstructor
public class PetService {
    private final PetDao petDao;
    private final OwnerDao ownerDao;
    private final PetMapper petMapper = new PetMapper();

    @Transactional
    public PetDto save(PetDto entity) {
        Pet pet = petMapper.toEntity(entity,
                getPets(entity.getFriendIds()),
                getOwner(entity.getOwner()));
        return petMapper.toDto(petDao.save(pet));
    }

    @Transactional
    public void deleteById(long id) {
        Pet pet = petDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pet wasn't found by id" + id));

        Owner owner = pet.getOwner();
        if (owner != null) {
            owner.getPets().remove(pet);
            pet.setOwner(null);
        }

        List<Pet> oldFriends = new ArrayList<>(pet.getFriends());
        for (Pet friend : oldFriends) {
            friend.getFriends().remove(pet);
            pet.getFriends().remove(friend);
        }
        petDao.saveAll(oldFriends);
        petDao.delete(pet);
    }

    @Transactional
    public void deleteByEntity(PetDto entity) {
        Pet pet = petMapper.toEntity(entity, getPets(entity.getFriendIds()), getOwner(entity.getOwner()));

        Owner owner = pet.getOwner();
        if (owner != null) {
            owner.getPets().remove(pet);
            pet.setOwner(null);
        }

        List<Pet> oldFriends = new ArrayList<>(pet.getFriends());
        for (Pet friend : oldFriends) {
            friend.getFriends().remove(pet);
            pet.getFriends().remove(friend);
        }
        petDao.saveAll(oldFriends);
        petDao.delete(pet);
    }

    @Transactional
    public void deleteAll() {
        petDao.deleteAll();
    }

    @Transactional
    public PetDto update(Long id, PetDto entity) {
        Pet pet = petDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pet not found with id: " + id));
        if (entity.getName() != null) pet.setName(entity.getName());
        if (entity.getColor() != null) pet.setColor(entity.getColor());
        if (entity.getBirthDate() != null) pet.setBirthDate(entity.getBirthDate());
        if (entity.getBreed() != null) pet.setBreed(entity.getBreed());
        if (entity.getOwner() != null) {
            Owner owner = getOwner(entity.getId());
            pet.setOwner(owner);
        }
        if (!entity.getFriendIds().isEmpty()) {
            List<Pet> oldFriends = pet.getFriends();
            for (Pet friend : oldFriends) {
                friend.getFriends().remove(pet);
                pet.getFriends().remove(friend);
            }
            pet.getFriends().clear();
            List<Pet> newFriends = petDao.findAllById(entity.getFriendIds());
            pet.getFriends().addAll(newFriends);
        }
        return petMapper.toDto(petDao.save(pet));
    }

    @Transactional
    public PetDto getById(long id) {
        Pet pet = petDao.findById(id).orElseThrow(() -> new EntityNotFoundException("Pet not found with id: " + id));
        return petMapper.toDto(pet);
    }

    @Transactional
    public List<PetDto> getAll() {
        return petDao.findAll().stream().map(petMapper::toDto).toList();
    }

    @Transactional
    public Page<PetDto> getPetsWithFilter(int page, int size, String name, LocalDate birthdate, String breed, Color color, Long ownerId, List<Long> friendIds) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Pet> petPage;
        if (friendIds != null && !friendIds.isEmpty()) {
            petPage = petDao.findByFriendsIdIn(friendIds, pageable);
        } else if (name != null && !name.isEmpty() && birthdate != null) {
            petPage = petDao.findByNameAndBirthDate(name, birthdate, pageable);
        } else if (name != null && !name.isEmpty()) {
            petPage = petDao.findByName(name, pageable);
        } else if (birthdate != null) {
            petPage = petDao.findByBirthDate(birthdate, pageable);
        } else if (breed != null) {
            petPage = petDao.findByBreed(breed, pageable);
        } else if (color != null) {
            petPage = petDao.findByColor(color, pageable);
        } else if (ownerId != null) {
            petPage = petDao.findByOwnerId(ownerId, pageable);
        } else {
            petPage = petDao.findAll(pageable);
        }
        List<PetDto> petDtos = petPage.getContent().stream().map(petMapper::toDto).toList();
        return new PageImpl<>(petDtos, pageable, petPage.getTotalElements());
    }

    @Transactional
    public boolean getIsPetOwner(Long petId, Long ownerId) {
        return petDao.existsByIdAndOwnerId(petId, ownerId);
    }

    private List<Pet> getPets(List<Long> petIds) {
        if (petIds == null) {
            return new ArrayList<>();
        }
        List<Pet> pets = new ArrayList<>();
        for (Long petId : petIds) {
            Pet pet = petDao.findById(petId)
                    .orElseThrow(() -> new EntityNotFoundException("Pet not found with id: " + petId));
            pets.add(pet);
        }
        return pets.stream().toList();
    }

    private Owner getOwner(Long id) {
        if (id == null) {
            return null;
        }
        return ownerDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found with id: " + id));
    }
}
