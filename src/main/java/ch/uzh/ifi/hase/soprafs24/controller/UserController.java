package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetFullDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
/*
 * @RestController. This is known as a stereotype annotation.
 * It provides hints for people reading the code and for Spring that the class
 * plays a specific role.
 * Makes this class to serve REST endpoints
 */
@RestController
public class UserController {

  private final UserService userService;

  UserController(UserService userService) {
    this.userService = userService;
  }

  // GET ALL USERS
  @GetMapping("/users")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<UserGetDTO> getAllUsers(@RequestHeader("Authorization") String token) {
    // Fetch all users in the internal representation
    List<User> users = this.userService.getUsers(token);
    List<UserGetDTO> userGetDTOs = new ArrayList<>();
    // Convert each user to the API representation
    for (User user : users) {
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
    }
    return userGetDTOs;
  }

  // GET DETAIL OF A USER
  @GetMapping("/users/{id}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public UserGetFullDTO getUser(@PathVariable Long id, @RequestHeader("Authorization") String myToken) {
    Long idToInspect = id;
    User userRetrievedToInspect = this.userService.getFullUser(idToInspect, myToken);
    return DTOMapper.INSTANCE.convertEntitytoUserGetFullDTO(userRetrievedToInspect);
  }

  // CREATE NEW USER
  @PostMapping("/users")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO, HttpServletResponse response) {
    // Convert API user to internal representation
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
    // Create user
    User createdUser = this.userService.createUser(userInput);
    // Add token in response headers
    response.addHeader("Authorization", createdUser.getToken());
    // Convert internal representation of user back to API
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

  // UPDATE USER
  @PutMapping("/users/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public UserGetFullDTO editProfile(@PathVariable Long id, @RequestBody UserPutDTO userPutDTO,
      @RequestHeader("Authorization") String token) {
    User userWithPendingChanges = DTOMapper.INSTANCE.convertUserPutDTOtoEntity(userPutDTO);
    User updatedUser = this.userService.editProfile(userWithPendingChanges, token, id);
    UserGetFullDTO userSentToClient = DTOMapper.INSTANCE.convertEntitytoUserGetFullDTO(updatedUser);
    return userSentToClient;
  }

  // LOGIN
  @PostMapping("/login")
  @ResponseStatus(HttpStatus.CREATED)
  @ResponseBody
  // HttpServletResponse response to return to the client the token
  public UserGetFullDTO login(@RequestBody UserPostDTO userPostDTO, HttpServletResponse response) {
    User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
    User userRetrieved = this.userService.logIn(userInput);
    // I convert the UserEntity to a format readable by the Client
    UserGetFullDTO userSentToClient = DTOMapper.INSTANCE.convertEntitytoUserGetFullDTO(userRetrieved);
    response.addHeader("Authorization", userRetrieved.getToken());
    return userSentToClient;
  }

  // LOGOUT
  @PutMapping("/logout")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public void logout(@RequestHeader("Authorization") String token) {
    this.userService.logOut(token);
  }
}
