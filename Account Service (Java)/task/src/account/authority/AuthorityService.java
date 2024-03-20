package account.authority;

import account.businesslayer.UserService;
import account.businesslayer.entity.Authority;
import account.businesslayer.exceptions.InvalidChangeException;
import account.businesslayer.exceptions.NotFoundException;
import account.businesslayer.request.RoleChangeRequest;
import account.persistencelayer.AuthorityRepository;
import account.persistencelayer.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthorityService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthorityRepository authorityRepository;

    public Set<Authority> setAuthority(){
        return userRepository.count() > 0 ?
                Set.of(getAuthoritybyRole("ROLE_USER")):Set.of(getAuthoritybyRole("ROLE_ADMINISTRATOR"));
    }

    public Authority getAuthoritybyRole(String role){
        return authorityRepository.findByRole(role)
        .orElseThrow(() -> new RuntimeException("Not found"));
    }

    public Set<Authority> modifyUserAuthority (Set<Authority> currentAuthority, RoleChangeRequest request) {
        Authority authorityToDelete = getAuthoritybyRole(request.role());
        return currentAuthority.stream()
                .filter(authority -> !authority.equals(authorityToDelete))
                .collect(Collectors.toSet());
    }

    public void validateNoRoleConflict(Set<Authority> currentAuthorities, Authority newAuthority) {
        for (Authority authority : currentAuthorities) {
            if (!authority.getRoleGroup().equalsIgnoreCase(newAuthority.getRoleGroup()))
                throw new InvalidChangeException("The user cannot combine administrative and business roles!");
        }
    }

    public void validateRoleExists(String role) {
        if (!authorityRepository.existsByRole(role)) throw new NotFoundException("Role not found!");
    }

    public void validateRoleRemoval(RoleChangeRequest request, Set<Authority> currentAuthorities) {
        if (request.role().equalsIgnoreCase("ROLE_ADMINISTRATOR")) {
            throw new InvalidChangeException("Can't remove ADMINISTRATOR role!");
        }
        if (!currentAuthorities.contains(getAuthoritybyRole(request.role()))) {
            throw new InvalidChangeException("The user does not have a role!");
        }
        if (currentAuthorities.size() < 2) {
            throw new InvalidChangeException("The user must have at least one role!");
        }
    }
}
