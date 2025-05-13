package ru.gerilovich;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.gerilovich.config.SecurityConfig;
import ru.gerilovich.controllers.PetController;
import ru.gerilovich.dto.PetDto;
import ru.gerilovich.models.Color;
import ru.gerilovich.models.auth.CustomUserDetails;
import ru.gerilovich.services.PetService;
import ru.gerilovich.services.auth.OwnerDetailsService;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PetController.class)
@Import({SecurityConfig.class, PetService.class})
public class PetControllerAdminTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PetService petService;

    @MockitoBean
    private OwnerDetailsService ownerDetailsService;


    private final CustomUserDetails userDetails = new CustomUserDetails(
            30L,
            "masha",
            "$2a$12$JNR4LEIEn11ZTLF23QUlLu9NlaU4wEsxtzEGc4Wn7XQtD5fjm78Ye",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
    );

    @BeforeEach
    void setUp() {
        when(petService.getIsPetOwner(anyLong(), anyLong()))
                .thenReturn(false);
        when(ownerDetailsService.loadUserByUsername(anyString()))
                .thenReturn(userDetails);
    }

    @Test
    @WithAnonymousUser
    void cannotGetCustomerIfNotAuthorized() throws Exception {
        mockMvc.perform(get("/api/pets/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getPetById_WhenAdmin_ShouldReturnPet() throws Exception {
        PetDto pet = createTestPet(999L);
        when(petService.getById(1L)).thenReturn(pet);

        mockMvc.perform(get("/api/pets/1")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_WhenAdmin_ShouldUpdatePet() throws Exception {
        PetDto updatedPetDto = new PetDto();
        updatedPetDto.setId(1L);
        updatedPetDto.setName("tralalelo tralalelovich");
        updatedPetDto.setBirthDate(LocalDate.of(2008, 9, 16));
        updatedPetDto.setBreed("Whale");
        updatedPetDto.setColor(Color.BLACK);
        updatedPetDto.setOwner(33L);
        updatedPetDto.setFriendIds(List.of(2239L, 552L));

        String requestBody = """
                {
                "name": "tralalelo tralalelovich",
                "birthDate": "2008-09-16",
                "breed": "Whale",
                "color": "BLACK",
                "owner": 33,
                "friendIds": [2239, 552]
                }
                """;

        when(petService.update(any(), any())).thenReturn(updatedPetDto);
        mockMvc.perform(put("/api/pets/1", updatedPetDto)
                        .with(user(userDetails))
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("tralalelo tralalelovich"))
                .andExpect(jsonPath("$.birthDate").value(LocalDate.of(2008, 9, 16).toString()))
                .andExpect(jsonPath("$.breed").value("Whale"))
                .andExpect(jsonPath("$.color").value("BLACK"))
                .andExpect(jsonPath("$.owner").value(33))
                .andExpect(jsonPath("$.friendIds").value(containsInAnyOrder(552, 2239)));
    }

    @Test
    void create_WhenAdmin_ShouldCreatePet() throws Exception {
        PetDto petDto = createTestPet(999L);
        String requestBody = """
                {
                "name": "tralalelo tralala",
                "birthDate": "2006-04-08",
                "breed": "Shark",
                "color": "WHITE",
                "owner": 999,
                "friendIds": [52, 239, 100]
                }
                """;
        when(petService.save(any(PetDto.class))).thenReturn(petDto);
        mockMvc.perform(post("/api/pets")
                        .with(user(userDetails))
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void getPetsWithFilters_WhenAdmin_ShouldReturnPets() throws Exception {
        PetDto petDto = createTestPet(999L);
        Page<PetDto> page = new PageImpl<>(List.of(petDto), PageRequest.of(0, 10), 1);

        Mockito.when(petService.getPetsWithFilter(anyInt(), anyInt(), any(), any(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/pets/filter")
                        .with(user(userDetails))
                        .param("page", "0")
                        .param("size", "10")
                        .param("name", "tralalelo tralala")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("tralalelo tralala"));
    }

    @Test
    void deleteById_WhenAdmin_ShouldDeletePet() throws Exception {
        long petId = 1L;
        Mockito.doNothing().when(petService).deleteById(petId);
        mockMvc.perform(delete("/api/pets/{id}", petId)
                        .with(user(userDetails)))
                .andExpect(status().isNoContent());
        Mockito.verify(petService, times(1)).deleteById(petId);
    }

    @Test
    void deleteByEntity_WhenAdmin_ShouldDeletePet() throws Exception {
        PetDto petDto = createTestPet(999L);
        String requestBody = """
                {
                "id": 1,
                "name": "tralalelo tralala",
                "birthDate": "2006-04-08",
                "breed": "Shark",
                "color": "WHITE",
                "owner": 999,
                "friendIds": [52, 239, 100]
                }
                """;
        Mockito.doNothing().when(petService).deleteByEntity(petDto);
        mockMvc.perform(delete("/api/pets")
                        .with(user(userDetails))
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Mockito.verify(petService, times(1)).deleteByEntity(
                argThat(dto -> dto.getId().equals(1L) && dto.getName().equals("tralalelo tralala")
                ));
    }

    @Test
    void deleteAll_WhenAdmin_ShouldDeleteAllPets() throws Exception {
        mockMvc.perform(delete("/api/pets/all")
                        .with(user(userDetails)))
                .andExpect(status().isOk());
    }


    private PetDto createTestPet(Long ownerId) {
        PetDto pet = new PetDto(
                "tralalelo tralala",
                LocalDate.of(2006, 4, 8),
                "Shark",
                Color.WHITE,
                ownerId,
                List.of(52L, 239L, 100L)
        );
        pet.setId(1L);
        return pet;
    }
}
