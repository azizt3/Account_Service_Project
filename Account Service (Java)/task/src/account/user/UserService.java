package account.user;

import account.BreachedPasswords;
import account.authority.AuthorityService;
import account.payment.dto.UpdateSuccessfulDto;
import account.authority.Authority;
import account.exceptionhandler.exception.InsufficientPasswordException;
import account.exceptionhandler.exception.InvalidChangeException;
import account.exceptionhandler.exception.NotFoundException;
import account.exceptionhandler.exception.UserExistsException;
import account.authority.request.RoleChangeRequest;
import account.user.dto.UserDeletedDto;
import account.user.dto.UserDto;
import account.user.request.UserRegistrationRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Bean
    BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(13); }
    UserRepository userRepository;
    AuthorityService authorityService;
    BreachedPasswords breachedPasswords;

    @Autowired
    public UserService(UserRepository userRepository, BreachedPasswords breachedPasswords,
                       AuthorityService authorityService) {
        this.userRepository = userRepository;
        this.breachedPasswords = breachedPasswords;
        this.authorityService = authorityService;
    }

    //Business Logic

    public ResponseEntity<UserDto[]> handleGetUsers(){
        if (userRepository.count() == 0) return ResponseEntity.ok().body(new UserDto[]{});
        UserDto[] allUsers =  buildUserDtoArray(userRepository.findAll());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(allUsers);
    }

    @Transactional
    public ResponseEntity<UserDto> register(UserRegistrationRequest newUser) {
        validateUniqueEmail(newUser.email());
        validateNewPassword(newUser.password());
        User user = buildUser(newUser);
        userRepository.save(user);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildUserDto(user));
    }

    public ResponseEntity<?> handleRoleChange(RoleChangeRequest request){
        User user = loadUser(request.user());
        authorityService.validateRoleExists(request.role());
        if (request.operation().equalsIgnoreCase("grant")) return handleRoleGrant(request, user);
        if (request.operation().equalsIgnoreCase("remove")) return handleRoleRemove(request, user);
        else throw new NotFoundException("");
    }

    private ResponseEntity<UserDto> handleRoleRemove(RoleChangeRequest request, User user) {
        Set<Authority> currentAuthorities = user.getAuthorities();
        authorityService.validateRoleRemoval(request, currentAuthorities);
        Set<Authority> newAuthorities = authorityService.modifyUserAuthority(currentAuthorities, request);

        user.setAuthorities(newAuthorities);
        userRepository.save(user);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildUserDto(user));
    }

    private ResponseEntity<?> handleRoleGrant(RoleChangeRequest request, User user) {
        Authority newAuthority = authorityService.getAuthoritybyRole(request.role());
        Set<Authority> currentAuthorities = user.getAuthorities();
        authorityService.validateNoRoleConflict(currentAuthorities, newAuthority);
        currentAuthorities.add(newAuthority);
        user.setAuthorities(currentAuthorities);
        userRepository.save(user);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(buildUserDto(user));
    }

    @Transactional
    public ResponseEntity<UpdateSuccessfulDto> updatePassword(String newPassword, UserAdapter user) {
        validatePasswordLength(newPassword);
        validateUniquePassword(newPassword, user.getPassword());
        breachedPasswords.validatePasswordBreached(newPassword);

        User updatedUser = loadUser(user.getEmail());
        updatedUser.setPassword(passwordEncoder().encode(newPassword));
        userRepository.save(updatedUser);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateSuccessfulDto(user.getEmail(), "The password has been updated successfully"));
    }

    @Transactional
    public ResponseEntity<UserDeletedDto> handleUserDelete(String email) {
        User user = loadUser(email);
        if (getRoles(user).contains("ROLE_ADMINISTRATOR")) {
            throw new InvalidChangeException("Can't remove ADMINISTRATOR role!");
        }
        userRepository.deleteByEmail(email);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UserDeletedDto(email, "Deleted successfully!"));
    }

    public User loadUser (String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found!"));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException(("")));
        return new UserAdapter(user, getUserAuthorities(user));
    }

    private Collection<? extends GrantedAuthority> getUserAuthorities(User user) {
        return getRoles(user).stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    //Validation Methods

    public void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email.toLowerCase())) throw new UserExistsException("User exist!");
    }

    public void validatePasswordLength(String newPassword) {
        if (newPassword.length() < 12) {
            throw new InsufficientPasswordException("Password length must be 12 chars minimum!");
        }
    }

    public void validateUniquePassword(String newPassword, String oldPassword) {
        if (passwordEncoder().matches(newPassword, oldPassword)) {
            throw new InsufficientPasswordException("The passwords must be different!");
        }
    }

    public void validateNewPassword(String newPassword) {
        validatePasswordLength(newPassword);
        breachedPasswords.validatePasswordBreached(newPassword);
    }

    void validateUserExists(String employee) {
        if (!userRepository.existsByEmail(employee.toLowerCase())){
            throw new NotFoundException("User not found!");
        }
    }

    //Helper Methods

    public List<String> getRoles(User user) {
        return user.getAuthorities()
            .stream()
            .map(authority -> authority.getRole())
            .sorted()
            .toList();
    }

    public User buildUser(UserRegistrationRequest newUser) {
        User user = new User(
                newUser.name(),
                newUser.lastname(),
                newUser.email().toLowerCase(),
                passwordEncoder().encode(newUser.password()),
                authorityService.setAuthority());
        return user;
    }

    public UserDto buildUserDto(User user) {
        return new UserDto(
            user.getId(),
            user.getName(),
            user.getLastName(),
            user.getEmail(),
            getRoles(user).toArray(new String[0]));
    }

    public UserDto[] buildUserDtoArray(List<User> users){
        return users.stream()
                .map(this::buildUserDto)
                .sorted(Comparator.comparing(UserDto::id))
                .toList()
                .toArray(new UserDto[0]);
    }
}
