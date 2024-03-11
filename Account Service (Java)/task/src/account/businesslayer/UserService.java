package account.businesslayer;

import account.BreachedPasswords;
import account.businesslayer.dto.UpdateSuccessfulDto;
import account.businesslayer.dto.UserAdapter;
import account.businesslayer.dto.UserDeletedDto;
import account.businesslayer.dto.UserDto;
import account.businesslayer.entity.Authority;
import account.businesslayer.entity.User;
import account.businesslayer.exceptions.InsufficientPasswordException;
import account.businesslayer.exceptions.InvalidChangeException;
import account.businesslayer.exceptions.NotFoundException;
import account.businesslayer.exceptions.UserExistsException;
import account.businesslayer.request.RoleChangeRequest;
import account.businesslayer.request.UserRegistrationRequest;
import account.persistencelayer.AuthorityRepository;
import account.persistencelayer.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthorityRepository authorityRepository;

    @Autowired
    BreachedPasswords breachedPasswords;

    public UserService(UserRepository userRepository, BreachedPasswords breachedPasswords, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.breachedPasswords = breachedPasswords;
        this.authorityRepository = authorityRepository;
    }

    //Validation Methods

    @Transactional
    public UserDeletedDto handleUserDelete(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User not found!"));
        if (isAdmin(user)) throw new InvalidChangeException("Can't remove ADMINISTRATOR role!");
        userRepository.deleteByEmail(email);
        return new UserDeletedDto(email, "Deleted successfully!");
    }

    private boolean isAdmin(User user) {
        List<String> roles = getRoles(user);
        return roles.contains("ROLE_ADMINISTRATOR");
    }

    public UserDto handleRoleChange(RoleChangeRequest roleChangeRequest){
        String role = "ROLE_"+roleChangeRequest.role();
        validateUserExists(roleChangeRequest.user());
        validateRoleExists(role);
        User user = loadUser(roleChangeRequest.user());
        if (roleChangeRequest.operation().equalsIgnoreCase("grant")) {
            return handleRoleGrant(roleChangeRequest, user);
        }
        if (roleChangeRequest.operation().equalsIgnoreCase("remove")) {
            return handleRoleRemove(roleChangeRequest, user);
        }
        else throw new NotFoundException("");
    }

    private UserDto handleRoleRemove(RoleChangeRequest roleChangeRequest, User user) {

        List<String> roles = getRoles(user);
        if (roles.size() < 2) throw new InvalidChangeException("The user must have at least one role!");
        if (!roles.contains(roleChangeRequest.role())) {
            throw new InvalidChangeException("The user does not have a role!");
        }
        if (roleChangeRequest.role().equalsIgnoreCase("ADMINISTRATOR")) {
            throw new InvalidChangeException("Can't remove ADMINISTRATOR role!");
        }

        Set<Authority> newAuthorities= roles.stream()
            .map(role -> authorityRepository.findByRole(role).orElseThrow())
            .collect(Collectors.toSet());

        user.setAuthorities(newAuthorities);
        userRepository.save(user);
        return buildUserDto(user);
    }

    private UserDto handleRoleGrant(RoleChangeRequest roleChangeRequest, User user) {
        Authority newAuthority = authorityRepository.findByRole("ROLE_" + roleChangeRequest.role())
            .orElseThrow();
        Set<Authority> authorities = user.getAuthorities();
        authorities.forEach(
            authority -> validateNoRoleConflict(authority.getRoleGroup(), newAuthority.getRoleGroup())
        );

        authorities.add(newAuthority);
        user.setAuthorities(authorities);
        userRepository.save(user);
        return buildUserDto(user);

    }

    public void validateNoRoleConflict(String currentGroup, String newGroup) {
        if (currentGroup.equalsIgnoreCase(newGroup)) {
            throw new InvalidChangeException("The user cannot combine administrative and business roles!");
        }
    }

    public void validateRoleExists(String role) {
        if (!authorityRepository.existsByRole(role)) throw new NotFoundException("Role not found!");
    }

    public void validateEmail(String email) {
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

    public void validatePasswordBreached(String newPassword) {
        int i = 0;
        while (i < breachedPasswords.getBreachedPasswords().size()) {
            if (passwordEncoder().matches(newPassword, breachedPasswords.getBreachedPasswords().get(i))) {
                throw new InsufficientPasswordException("The password is in the hacker's database!");
            } i++;
        }
    }

    public void validateNewPassword(String newPassword) {
        validatePasswordLength(newPassword);
        validatePasswordBreached(newPassword);
    }

    void validateUserExists(String employee) {
        if (!userRepository.existsByEmail(employee.toLowerCase())){
            throw new UsernameNotFoundException("User not found!");
        }
    }
    public void validatePasswordUpdate(String newPassword, String oldPassword) {
        validatePasswordLength(newPassword);
        validateUniquePassword(newPassword, oldPassword);
        validatePasswordBreached(newPassword);
    }

    //Authority Related Methods

    public Authority getAuthoritybyRole(String role){
        return authorityRepository.findByRole(role)
        .orElseThrow(() -> new RuntimeException("Not found"));
    }

    public Set<Authority> setAuthority(){
        return userRepository.count() > 0 ?
            Set.of(getAuthoritybyRole("ROLE_USER")):Set.of(getAuthoritybyRole("ROLE_ADMINISTRATOR"));
    }

    //Business Logic


    public User loadUser (String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("Not Found!"));
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException(("")));
        return new UserAdapter(user, getUserAuthorities(user));
    }

    public UserDto[] handleGetUsers(){
        List<UserDto> userArr = new ArrayList<>();
        if (userRepository.count() == 0) return new UserDto[]{};

        userRepository.findAll().forEach(user -> userArr.add(buildUserDto(user)));
        List<UserDto> sortedUsers =  userArr.stream()
            .sorted(Comparator.comparing(UserDto::id))
            .toList();
        return sortedUsers.toArray(new UserDto[0]);
    }

    private Collection<? extends GrantedAuthority> getUserAuthorities(User user) {
        return getRoles(user).stream()
            .map(role -> new SimpleGrantedAuthority(role))
            .collect(Collectors.toCollection(ArrayList::new));

        /*Set<Authority> userAuthority = user.getAuthority();
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(userAuthority.getRole()));
        return authorities;*/
    }

    private List<String> getRoles(User user) {
        return user.getAuthorities()
            .stream()
            .map(authority -> authority.getRole())
            .toList();
    }

    @Transactional
    public User register(UserRegistrationRequest newUser) {
        User user = new User(
                newUser.name(),
                newUser.lastname(),
                newUser.email().toLowerCase(),
                passwordEncoder().encode(newUser.password()),
                setAuthority());
        userRepository.save(user);
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


    @Transactional
    public User updatePassword(String newPassword, UserAdapter user) {
        User updatedUser = userRepository
            .findByEmail(user.getEmail().toLowerCase())
            .orElseThrow(() -> new UsernameNotFoundException(""));
        updatedUser.setPassword(passwordEncoder().encode(newPassword));
        userRepository.save(updatedUser);
        return updatedUser;
    }


}
