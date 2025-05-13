package ru.gerilovich.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.gerilovich.models.Color;
import ru.gerilovich.models.Pet;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PetDao extends JpaRepository<Pet, Long> {
    Page<Pet> findByName(String name, Pageable pageable);

    Page<Pet> findByBreed(String breed, Pageable pageable);

    Page<Pet> findByColor(Color color, Pageable pageable);

    Page<Pet> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Pet> findByBirthDate(LocalDate birthDate, Pageable pageable);

    Page<Pet> findByFriendsIdIn(List<Long> petIds, Pageable pageable);

    Page<Pet> findByNameAndBirthDate(String name, LocalDate birthDate, Pageable pageable);

    boolean existsByIdAndOwnerId(Long id, Long ownerId);
}
