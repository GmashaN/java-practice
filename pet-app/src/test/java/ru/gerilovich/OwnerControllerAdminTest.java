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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.gerilovich.config.SecurityConfig;
import ru.gerilovich.controllers.OwnerController;
import ru.gerilovich.dto.OwnerDto;
import ru.gerilovich.models.Role;
import ru.gerilovich.models.auth.CustomUserDetails;
import ru.gerilovich.services.OwnerService;
import ru.gerilovich.services.auth.OwnerDetailsService;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OwnerController.class)
@Import(SecurityConfig.class)
public class OwnerControllerAdminTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerService ownerService;

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
        when(ownerDetailsService.loadUserByUsername(anyString()))
                .thenReturn(userDetails);
    }

    @Test
    void findAll_WhenAdmin_ShouldReturnAllOwners() throws Exception {
        Mockito.when(ownerService.getAll()).thenReturn(getOwners());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/owners")
                        .with(user(userDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

    }

    @Test
    void findById_WhenAdmin_ShouldReturnOwner() throws Exception {
        Mockito.when(ownerService.getById(1L)).thenReturn(getOwners().getFirst());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/owners/1")
                        .with(user(userDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("masha"))
                .andExpect(jsonPath("$.birthDate").value("2006-04-08"))
                .andExpect(jsonPath("$.petIds").value(containsInAnyOrder(52, 239, 100)));

    }

    @Test
    void update_WhenAdmin_ShouldUpdateOwner() throws Exception {
        OwnerDto updatedOwnerDto = new OwnerDto();
        updatedOwnerDto.setId(1L);
        updatedOwnerDto.setName("mashina");
        updatedOwnerDto.setBirthDate(LocalDate.of(2008, 9, 16));
        updatedOwnerDto.setPetIds(List.of(2239L, 552L));

        String json = """
                {
                "name": "mashina",
                "birthDate": "2008-09-16",
                "petIds": [2239, 552]
                }
                """;

        Mockito.when(ownerService.update(any(), any(OwnerDto.class))).thenReturn(updatedOwnerDto);
        mockMvc.perform(put("/api/owners/1", updatedOwnerDto)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("mashina"))
                .andExpect(jsonPath("$.birthDate").value(LocalDate.of(2008, 9, 16).toString()))
                .andExpect(jsonPath("$.petIds").value(containsInAnyOrder(552, 2239)));
    }

    @Test
    void create_WhenAdmin_ShouldCreateOwner() throws Exception {
        OwnerDto ownerDto = getOwners().getFirst();
        String requestBody = """
                {
                "name": "masha",
                "birthdate": "2006-04-08",
                "petIds": [52, 239, 100]
                }
                """;
        Mockito.when(ownerService.save(any(OwnerDto.class))).thenReturn(ownerDto);
        mockMvc.perform(post("/api/owners")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetails)))
                .andExpect(status().isCreated());
    }

    @Test
    void getOwnersWithFilters_WhenAdmin_ShouldReturnOwners() throws Exception {
        OwnerDto ownerDto = getOwners().getFirst();
        Page<OwnerDto> page = new PageImpl<>(List.of(ownerDto), PageRequest.of(0, 10), 1);

        Mockito.when(ownerService.getOwnersWithFilter(anyInt(), anyInt(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/owners/filter")
                        .param("page", "0")
                        .param("size", "10")
                        .param("name", "masha")
                        .param("birthDate", "2006-04-08")
                        .param("petIds", "52")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetails))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("masha"));
    }

    @Test
    void deleteById_WhenAdmin_ShouldDeleteOwner() throws Exception {
        long ownerId = 1L;
        Mockito.doNothing().when(ownerService).deleteById(ownerId);
        mockMvc.perform(delete("/api/owners/{id}", ownerId).with(user(userDetails)))
                .andExpect(status().isNoContent());
        Mockito.verify(ownerService, times(1)).deleteById(ownerId);
    }

    @Test
    void deleteByEntity_WhenAdmin_ShouldDeleteOwner() throws Exception {
        OwnerDto ownerDto = getOwners().getFirst();
        String requestBody = """
                {
                "id": 1,
                "name": "masha",
                "birthdate": "2006-04-08",
                "petIds": [52, 239, 100]
                }
                """;
        Mockito.doNothing().when(ownerService).deleteByEntity(ownerDto);
        mockMvc.perform(delete("/api/owners")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetails)))
                .andExpect(status().isNoContent());
        Mockito.verify(ownerService, times(1)).deleteByEntity(
                argThat(dto -> dto.getId().equals(1L) && dto.getName().equals("masha")
                ));
    }

    @Test
    void deleteAll_WhenAdmin_ShouldDeleteAllOwners() throws Exception {
        mockMvc.perform(delete("/api/owners/all").with(user(userDetails)))
                .andExpect(status().isOk());
    }

    private List<OwnerDto> getOwners() {
        OwnerDto one = new OwnerDto(
                "masha",
                LocalDate.of(2006, 4, 8),
                List.of(52L, 239L, 100L),
                Role.USER,
                "$2a$12$JNR4LEIEn11ZTLF23QUlLu9NlaU4wEsxtzEGc4Wn7XQtD5fjm78Ye");
        one.setId(1L);

        OwnerDto two = new OwnerDto(
                "give100points",
                LocalDate.now(),
                List.of(52L, 239L, 100L),
                Role.USER,
                "$2a$12$JNR4LEIEn11ZTLF23QUlLu9NlaU4wEsxtzEGc4Wn7XQtD5fjm78Ye");
        two.setId(2L);
        return List.of(one, two);
    }

}