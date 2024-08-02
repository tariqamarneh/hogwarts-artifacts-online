package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.hogwartsartifactsonline.hogwartsuser.dto.UserDto;
import edu.tcu.cs.hogwartsartifactsonline.system.StatusCode;
import edu.tcu.cs.hogwartsartifactsonline.system.exception.ObjectNotFoundException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    List<HogwartsUser> users;

    @Value("${api.endpoint.base-url}")
    String baseUrl;

    @BeforeEach
    void setUp() {
        this.users = new ArrayList<>();

        HogwartsUser u1 = new HogwartsUser();
        u1.setId(1);
        u1.setUsername("john");
        u1.setPassword("123456");
        u1.setEnabled(true);
        u1.setRoles("admin user");
        this.users.add(u1);

        HogwartsUser u2 = new HogwartsUser();
        u2.setId(2);
        u2.setUsername("eric");
        u2.setPassword("654321");
        u2.setEnabled(true);
        u2.setRoles("user");
        this.users.add(u2);

        HogwartsUser u3 = new HogwartsUser();
        u3.setId(2);
        u3.setUsername("tom");
        u3.setPassword("qwerty");
        u3.setEnabled(false);
        u3.setRoles("user");
        this.users.add(u3);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testFindAllUsersSuccess() throws Exception {
        given(this.userService.findAll()).willReturn(this.users);

        this.mockMvc.perform(get(this.baseUrl + "/users").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find All Success"))
                .andExpect(jsonPath("$.data", Matchers.hasSize(this.users.size())))
                .andExpect(jsonPath("$.data[0].id").value(this.users.get(0).getId()))
                .andExpect(jsonPath("$.data[0].username").value(this.users.get(0).getUsername()))
                .andExpect(jsonPath("$.data[1].id").value(this.users.get(1).getId()))
                .andExpect(jsonPath("$.data[1].username").value(this.users.get(1).getUsername()));
    }

    @Test
    void testFindUserByIdSuccess() throws Exception {
        given(this.userService.findById(2)).willReturn(this.users.get(2));

        this.mockMvc.perform(get(this.baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Find One Success"))
                .andExpect(jsonPath("$.data.id").value(this.users.get(2).getId()))
                .andExpect(jsonPath("$.data.username").value(this.users.get(2).getUsername()));
    }

    @Test
    void testFindUserByIdNotFound() throws Exception {
        given(this.userService.findById(5)).willThrow(new ObjectNotFoundException("user", 5));

        this.mockMvc.perform(get(this.baseUrl + "/users/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testAddUserSuccess() throws Exception {
        HogwartsUser user = new HogwartsUser();
        user.setId(4);
        user.setUsername("lily");
        user.setPassword("123456");
        user.setEnabled(true);
        user.setRoles("admin user");

        String json = objectMapper.writeValueAsString(user);

        user.setId(4);

        given(this.userService.save(Mockito.any(HogwartsUser.class))).willReturn(user);

        this.mockMvc.perform(post(this.baseUrl + "/users").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Add Success"))
                .andExpect(jsonPath("$.data.id").value(user.getId()))
                .andExpect(jsonPath("$.data.username").value(user.getUsername()))
                .andExpect(jsonPath("$.data.enabled").value(user.isEnabled()))
                .andExpect(jsonPath("$.data.roles").value(user.getRoles()));
    }

    @Test
    void testUpdateUserSuccess() throws Exception {
        UserDto userDto = new UserDto(3, "tom123", false, "user");
        HogwartsUser updatedUser = new HogwartsUser();
        updatedUser.setId(3);
        updatedUser.setUsername("tom123");
        updatedUser.setEnabled(false);
        updatedUser.setRoles("user");

        String json = this.objectMapper.writeValueAsString(userDto);

        given(this.userService.update(eq(3), Mockito.any(HogwartsUser.class))).willReturn(updatedUser);

        this.mockMvc.perform(put(this.baseUrl + "/users/3").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Update Success"))
                .andExpect(jsonPath("$.data.id").value(updatedUser.getId()))
                .andExpect(jsonPath("$.data.username").value(updatedUser.getUsername()))
                .andExpect(jsonPath("$.data.enabled").value(updatedUser.isEnabled()))
                .andExpect(jsonPath("$.data.roles").value(updatedUser.getRoles()));
    }

    @Test
    void testUpdateUserErrorWithNonExistentId() throws Exception {
        given(this.userService.update(eq(5), Mockito.any(HogwartsUser.class))).willThrow(new ObjectNotFoundException("user", 5));
        UserDto userDto = new UserDto(5, "tom123", false, "user");
        String json = this.objectMapper.writeValueAsString(userDto);

        this.mockMvc.perform(put(this.baseUrl + "/users/5").contentType(MediaType.APPLICATION_JSON).content(json).accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteUserSuccess() throws Exception {
        doNothing().when(this.userService).delete(2);

        this.mockMvc.perform(delete(this.baseUrl + "/users/2").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.message").value("Delete Success"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testDeleteUserErrorWithNonExistentId() throws Exception {
        doThrow(new ObjectNotFoundException("user", 5)).when(this.userService).delete(5);

        this.mockMvc.perform(delete(this.baseUrl + "/users/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Could not find user with Id 5 :("))
                .andExpect(jsonPath("$.data").isEmpty());
    }

}