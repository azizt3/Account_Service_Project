package account.user;

import account.authority.request.RoleChangeRequest;
import account.user.request.PasswordChangeRequest;
import account.user.request.UserRegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping(path = "/api/admin/user/")
    public ResponseEntity<?> getUsers(@AuthenticationPrincipal UserAdapter user){
        return userService.handleGetUsers();
    }
    @PostMapping(path = "/api/auth/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserRegistrationRequest newUser) {
        return userService.register(newUser);
    }
    @PutMapping(path = "/api/admin/user/role")
    public ResponseEntity<?> setRoles(@RequestBody RoleChangeRequest request){
        return userService.handleRoleChange(request);
    }
    @PostMapping(path = "/api/auth/changepass")
    public ResponseEntity<?> changePass(
        @RequestBody PasswordChangeRequest newPassword, @AuthenticationPrincipal UserAdapter user) {
        return userService.updatePassword(newPassword.password(), user);
    }
    @DeleteMapping(path = "/api/admin/user/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email){
        return userService.handleUserDelete(email);
    }
}
