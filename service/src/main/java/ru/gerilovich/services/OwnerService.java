package ru.gerilovich.services;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gerilovich.dao.OwnerDao;
import ru.gerilovich.dto.OwnerDto;
import ru.gerilovich.mappers.OwnerMapper;
import ru.gerilovich.models.Owner;
import ru.gerilovich.models.Pet;
import ru.gerilovich.services.exceptions.DuplicateOwnerException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerService {
    private final OwnerDao ownerDao;
    private final OwnerMapper ownerMapper = new OwnerMapper();
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OwnerDto save(OwnerDto entity) {
        if (ownerDao.existsByName(entity.getName())) {
            throw new DuplicateOwnerException("Owner already exists with name " + entity.getName());
        }
        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        Owner owner = ownerMapper.toEntity(entity, ownerDao.findPetsByIds(entity.getPetIds()));
        Owner savedOwner = ownerDao.save(owner);
        return ownerMapper.toDto(savedOwner);
    }

    @Transactional
    public void deleteById(Long id) {
        try {
            ownerDao.deleteById(id);
        } catch (IllegalArgumentException e) {
            throw new EntityNotFoundException("Owner wasn't found by id" + id);
        }
    }

    @Transactional
    public void deleteByEntity(OwnerDto entity) {
        Owner owner = ownerMapper.toEntity(entity, getPetsByOwnerId(entity.getId()));
        ownerDao.delete(owner);
    }

    @Transactional
    public void deleteAll() {
        ownerDao.deleteAll();
    }

    @Transactional
    public OwnerDto update(Long id, OwnerDto entity) {
        Owner owner = ownerDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found with id: " + id));
        if (entity.getName() != null) owner.setName(entity.getName());
        if (entity.getBirthDate() != null) owner.setBirthDate(entity.getBirthDate());
        if (entity.getPassword() != null) owner.setPassword(passwordEncoder.encode(entity.getPassword()));

        if (!entity.getPetIds().isEmpty()) {
            owner.getPets().forEach(pet -> pet.setOwner(null));
            owner.getPets().clear();
            List<Pet> pets = ownerDao.findPetsByIds(entity.getPetIds());
            for (Pet pet : pets) {
                pet.setOwner(owner);
            }
            owner.getPets().addAll(pets);
        }
        return ownerMapper.toDto(ownerDao.save(owner));
    }

    @Transactional
    public OwnerDto getById(long id) {
        Owner owner = ownerDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found with id: " + id));
        return ownerMapper.toDto(owner);
    }

    @Transactional
    public List<OwnerDto> getAll() {
        return ownerDao.findAll().stream().map(ownerMapper::toDto).toList();
    }


    @Transactional
    public Page<OwnerDto> getOwnersWithFilter(int page, int size, String name, LocalDate birthdate, List<Long> petIds) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Owner> ownerPage;
        if (petIds != null && !petIds.isEmpty()) {
            ownerPage = ownerDao.findByPetsIdIn(petIds, pageable);
        } else if (name != null && !name.isEmpty()) {
            ownerPage = ownerDao.findByName(name, pageable);
        } else if (birthdate != null) {
            ownerPage = ownerDao.findByBirthDate(birthdate, pageable);
        } else {
            ownerPage = ownerDao.findAll(pageable);
        }
        List<OwnerDto> ownerDtos = ownerPage.getContent().stream().map(ownerMapper::toDto).toList();
        return new PageImpl<>(ownerDtos, pageable, ownerPage.getTotalElements());
    }

    private List<Pet> getPetsByOwnerId(Long id) {
        List<Pet> petsByOwnerId = ownerDao.getPetsByOwnerId(id);
        if (petsByOwnerId.isEmpty()) {
            return new ArrayList<>();
        }
        return petsByOwnerId.stream().toList();
    }

}
