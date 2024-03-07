package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetFullDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    // In the context of UserControllerTest, this means that when you're testing
    // methods in UserController,
    // any calls to userService will go to the mock instance instead of the real
    // UserService.
    // This allows you to set up expectations and return values for userService
    // methods,
    // making it easier to test the UserController methods in isolation.
    @MockBean
    private UserService userService;

    @Test
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("firstname@lastname");
        user.setStatus(UserStatus.OFFLINE);

        List<User> allUsers = Collections.singletonList(user);

        // this mocks the UserService -> we define above what the userService should
        // return when getUsers() is called
        given(userService.getUsers(Mockito.anyString())).willReturn(allUsers);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", Mockito.anyString());

        // then: this is an emulation of a Get Request
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                // needed to convert the long to an int with intValue() to avoid type mismatch
                .andExpect(jsonPath("$[0].id", is(user.getId().intValue())))
                .andExpect(jsonPath("$[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
    }

    // Test n°1: POST Add User - Success
    @Test
    public void createUser_validInput_userCreated() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setPassword("testPassword");
        user.setToken("testToken");
        user.setCreationDate(LocalDateTime.now());
        user.setStatus(UserStatus.ONLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");

        // Mockito.any(): It's an argument matcher that matches any argument of any type
        given(userService.createUser(Mockito.any())).willReturn(user);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    // QUESTION TRYING FIRST POST STATUS 201
    // Test n°2: POST Add User - Error
    @Test
    public void addUser_unValidInput_returnConflict() throws Exception {

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("testPassword");

        // Now the real test, lets add the second user with the same username
        given(userService.createUser(Mockito.any())).willThrow(
                new ResponseStatusException(HttpStatus.CONFLICT,
                        "Add User failed because username testUsername already exists."));

        MockHttpServletRequestBuilder errorPostRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        // then
        mockMvc.perform(errorPostRequest)
                .andExpect(status().isConflict());
    }

    // Test n°3: GET - Success, retrieve user profile with userId
    @Test
    public void givenUser_whenGetUser_thenReturnUserGetFullDTO() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("Username");
        user.setStatus(UserStatus.OFFLINE);
        user.setCreationDate(LocalDateTime.now());
        user.setBirthdayDate(null);

        given(userService.getFullUser(Mockito.anyLong(), Mockito.anyString())).willReturn(user);

        // when
        MockHttpServletRequestBuilder getRequest = get("/users/{id}", Mockito.anyLong())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", Mockito.anyString());

        // when + then
        mockMvc.perform(getRequest).andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
                .andExpect(jsonPath("$.creationDate", is(user.getCreationDate().toString())))
                .andExpect(jsonPath("$.birthdayDate", is(user.getBirthdayDate())));
    }

    // Test n°4: GET ID - Error, user with userId was not found
    @Test
    public void givenUser_whenGetUser_thenReturnNotFound() throws Exception {

        given(userService.getFullUser(Mockito.anyLong(), Mockito.anyString())).willThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No user found with the given ID."));

        MockHttpServletRequestBuilder errorGetRequest = get("/users/{id}", Mockito.anyLong())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", Mockito.anyString());

        // then
        mockMvc.perform(errorGetRequest)
                .andExpect(status().isNotFound());

    }

    // Test n°5: PUT Edit User - Success
    @Test
    public void editUser_validInput_userUpdated() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("usernameChanged");
        // user.setBirthdayDate(LocalDate.of(2022, 12, 31));

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("usernameChanged");
        // userPutDTO.setBirthdayDate(LocalDate.of(2022, 12, 30));

        // Mockito.any(): It's an argument matcher that matches any argument of any type
        // When userService.editProfile is called with any User, String, and Long, then
        // return user
        // This behavior will be used in the test to simulate the behavior of
        // userService without actually calling the real editProfile method.
        given(userService.editProfile(user, "test", 1L)).willReturn(user);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder putRequest = put("/users/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "test")
                .content(asJsonString(userPutDTO));

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());
    }
    // quando noi mandiamo questa richiesta, la funzione ritorna un'istanza
    // UserPutDTO, e grazie a @Responsestatus un NoContent.
    // ora nella asserzione ci aspettiamo di poter esaminare la risposta e
    // verificare che nel json l'username è cambiato
    // mi chiedo se c'è qualche errore in termini di richiesta, perchè il test va in
    // 204, quindi l'http response è corretto

    // QUESTION TRYING FIRST POST STATUS 201
    // Test n°6: PUT Edit user - Error: user with userId was not found
    @Test
    public void editUser_unValidInput_returnUserNotFound() throws Exception {
        // given
        // User user = new User();
        // user.setId(1L);
        // user.setUsername("testUsername");
        // user.setToken("ciao");

        UserPutDTO userPutDTO = new UserPutDTO();
        userPutDTO.setUsername("usernameChanged");
        //userPutDTO.setBirthdayDate(LocalDateTime.of(2022, 12, 31, 23, 59, 59, 0));

        // Mockito.any(): It's an argument matcher that matches any argument of any type
        // When userService.editProfile is called with any User, String, and Long, then
        // return user
        // This behavior will be used in the test to simulate the behavior of
        // userService without actually calling the real editProfile method.
        // given(userService.editProfile(user, user.getToken(), 2L)).willReturn(user);
        
        // COPILOT dice di usare questo. è una cagata giusto?
        given(userService.editProfile(Mockito.any(User.class), Mockito.anyString(), Mockito.eq(200L)))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder errorPutRequest = put("/users/{id}", 200L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "asdasd")
                .content(asJsonString(userPutDTO));
        // then
        mockMvc.perform(errorPutRequest)
                .andExpect(status().isNotFound());
    }

    /**
     * Helper Method to convert userPostDTO into a JSON string such that the input
     * can be processed
     * Input will look like this: {"name": "Test User", "username": "testUsername"}
     * 
     * @param object
     * @return string
     */
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }
}

/*
 * // Test n°5: PUT Edit User - Success
 * 
 * @Test
 * public void editUser_validInput_userUpdated() throws Exception {
 * // given
 * User user = new User();
 * user.setId(1L);
 * user.setUsername("testUsername");
 * user.setBirthdayDate(LocalDateTime.of(2022, 12, 31, 23, 59, 59, 0));
 * 
 * UserPutDTO userPutDTO = new UserPutDTO();
 * userPutDTO.setUsername("testUsername");
 * userPutDTO.setBirthdayDate(LocalDateTime.of(2022, 12, 31, 23, 59, 59, 0));
 * 
 * // Mockito.any(): It's an argument matcher that matches any argument of any
 * type
 * given(userService.editProfile(Mockito.any(), Mockito.anyString(),
 * Mockito.anyLong())).willReturn(user);
 * 
 * // when/then -> do the request + validate the result
 * MockHttpServletRequestBuilder putRequest = put("/users/{id}",
 * Mockito.anyLong())
 * .contentType(MediaType.APPLICATION_JSON)
 * .header("Authorization", Mockito.anyString())
 * .content(asJsonString(userPutDTO));
 * 
 * // then
 * mockMvc.perform(putRequest)
 * .andExpect(status().isNoContent())
 * .andExpect(jsonPath("$.username", is(user.getUsername())))
 * .andExpect(jsonPath("$.birthdayDate",
 * is(user.getBirthdayDate().toString())));
 * }
 * 
 * // QUESTION TRYING FIRST POST STATUS 201
 * // Test n°6: PUT Edit user - Error: user with userId was not found
 * 
 * @Test
 * public void editUser_unValidInput_returnUserNotFound() throws Exception {
 * 
 * UserPutDTO userPutDTO = new UserPutDTO();
 * userPutDTO.setUsername("testUsername");
 * userPutDTO.setBirthdayDate(LocalDateTime.of(2022, 12, 31, 23, 59, 59, 0));
 * 
 * // Now the real test, lets add the second user with the same username
 * given(userService.editProfile(Mockito.any(), Mockito.anyString(),
 * Mockito.anyLong())).willThrow(
 * new ResponseStatusException(HttpStatus.NOT_FOUND,
 * "No user found with the given ID."));
 * 
 * MockHttpServletRequestBuilder errorPutRequest = put("/users/{id}",
 * Mockito.anyLong())
 * .contentType(MediaType.APPLICATION_JSON)
 * .header("Authorization", Mockito.anyString())
 * .content(asJsonString(userPutDTO));
 * 
 * // then
 * mockMvc.perform(errorPutRequest)
 * .andExpect(status().isNotFound());
 * }
 */