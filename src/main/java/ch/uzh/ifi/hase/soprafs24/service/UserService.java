package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {
  // QUESTION che è sta roba? vedi il debug.log
  private final Logger log = LoggerFactory.getLogger(UserService.class);
  // VARIABLE DECLARATION
  private final UserRepository userRepository;

  // VARIABLE INSTANCIATION
  // when is first instanciated Userservice is initialized passing an istance of a
  // userreposiroty.
  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // GET ALL USERS
  public List<User> getUsers(String myToken) {
    tokenExistance(myToken);
    return this.userRepository.findAll();
  }

  // GET USER DETAILS
  public User getFullUser(Long idToInspect, String myToken) {
    tokenExistance(myToken);
    User userToInspect = this.userRepository.findUserById(idToInspect);

    if (userToInspect == null){
     throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("The user with userId: %d was not found.", idToInspect));}
    return userToInspect;
  }

  // CREATE NEW USER
  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.ONLINE);
    newUser.setCreationDate(LocalDateTime.now());
    checkIfUserExists(newUser);
    // saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = this.userRepository.save(newUser);
    this.userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  // UPDATE USER
  public User editProfile(User userWithPendingChanges, String tokenProvidedbyClient, Long id) {
    tokenExistance(tokenProvidedbyClient);
    User userToEdit = this.userRepository.findUserById(id);
    if (userToEdit == null) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("The user with userId: %d was not found.", id));

    }
    tokenValidation(userToEdit, tokenProvidedbyClient, "Token is invalid.");
    userToEdit.setUsername(userWithPendingChanges.getUsername());
    userToEdit.setBirthdayDate(userWithPendingChanges.getBirthdayDate());
    return userToEdit;
  }

  // LOGIN
  public User logIn(User userLogin) {
    // I need to send to the repository only the username and not the full user
    User userRetrieved = this.userRepository.findByUsername(userLogin.getUsername());

    if (userRetrieved == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The username you provided was not found in the database."));
    }
    // this is the password of the user sent by the client
    String userLoginPassword = userLogin.getPassword();
    // this is the password of the user retrieved by the username provided
    String userRetrievedPassword = userRetrieved.getPassword();

    // in this way the exception (wrong password) is catched in the if statement
    if (!userLoginPassword.equals(userRetrievedPassword)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("The password was not correct."));
    }

    userRetrieved.setToken(UUID.randomUUID().toString());
    userRetrieved.setStatus(UserStatus.ONLINE);
    this.userRepository.saveAndFlush(userRetrieved);
    return userRetrieved;
  }

  // LOGOUT
  public void logOut(String token) {
    tokenExistance(token);
    User userRetrieved = this.userRepository.findUserByToken(token);
    if (userRetrieved == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Can't find the user."));
    }
    userRetrieved.setToken(null);
    userRetrieved.setStatus(UserStatus.OFFLINE);
    this.userRepository.save(userRetrieved);
  }

  // Check if the token is of the right user
  private Boolean tokenValidation(User user, String tokenProvidedbyClient, String errorMessage) {
    if (!tokenProvidedbyClient.equals(user.getToken())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
    }
    return true;
  }

  // Check if a token is valid
  private void tokenExistance(String token) {
    if (this.userRepository.findUserByToken(token) == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You don't have a valid token.");
    }
  }

  /**
   * This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User
   */
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = this.userRepository.findByUsername(userToBeCreated.getUsername());

    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Add User failed because username %s already exists.", userByUsername.getUsername()));
    }
  }

}
