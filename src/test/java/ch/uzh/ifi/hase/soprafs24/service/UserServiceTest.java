package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder.In;

public class UserServiceTest {
  //Here we are mocking a UserRepository's instance in order to not apply modification
  //on the real data while we test
  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  private User createTestUser;

  @BeforeEach
  public void setup() {
    MockitoAnnotations.openMocks(this);

    // given
    createTestUser = new User();
    createTestUser.setUsername("testUsername");
    createTestUser.setPassword("testPassword");
    createTestUser.setStatus(UserStatus.ONLINE);
    createTestUser.setToken(UUID.randomUUID().toString());
    createTestUser.setCreationDate(LocalDateTime.now());

    // when -> any object is being save in the userRepository -> return the dummy
    // createTestUser
    Mockito.when(userRepository.save(Mockito.any())).thenReturn(createTestUser);
  }

  @Test
  public void createUser_validInputs_success() {

    // when -> any object is being save in the userRepository -> return the dummy
    // createTestUser
    User createdUser = userService.createUser(createTestUser);

    // then
    Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());

    assertEquals(createTestUser.getUsername(), createdUser.getUsername());
    assertEquals(createTestUser.getPassword(), createdUser.getPassword());
    assertNotNull(createdUser.getToken());
    assertEquals(UserStatus.ONLINE, createdUser.getStatus());
    assertNotNull(createdUser.getCreationDate());
  }
//  In the implementation according to the specified requirements, the name field has been removed. 
// Therefore, it is not possible to perform a test to verify that two users with the same name cannot exist.
/*   @Test
  public void createUser_duplicateName_throwsException() {
    // given -> a first user has already been created
    userService.createUser(createTestUser);

    // when -> setup additional mocks for UserRepository
    //that when the findByUsername method of userRepository is called with any string as the argument, it should return null.
    Mockito.when(userRepository.findUserById(Mockito.any())).thenReturn(createTestUser);
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(createTestUser));
  } */

  @Test
  public void createUser_duplicateInputs_throwsException() {
    // given -> a first user has already been created
    userService.createUser(createTestUser);

    // when -> setup additional mocks for UserRepository
    Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(createTestUser);

    // then -> attempt to create second user with same user -> check that an error
    // is thrown
    assertThrows(ResponseStatusException.class, () -> userService.createUser(createTestUser));
  }

}