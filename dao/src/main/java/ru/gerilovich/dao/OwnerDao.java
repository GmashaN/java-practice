package ru.gerilovich.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.gerilovich.models.Owner;
import ru.gerilovich.models.Pet;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerDao extends JpaRepository<Owner, Long> {
    @Query("SELECT p FROM Pet p WHERE p.owner.id = :ownerId")
    List<Pet> getPetsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT p FROM Pet p WHERE p.id IN :petIds")
    List<Pet> findPetsByIds(@Param("petIds") List<Long> petIds);

    Page<Owner> findByName(String name, Pageable pageable);

    Optional<Owner> findByName(String name);

    boolean existsByName(String name);

    Page<Owner> findByBirthDate(LocalDate birthDate, Pageable pageable);

    Page<Owner> findByPetsIdIn(List<Long> petIds, Pageable pageable);
}
