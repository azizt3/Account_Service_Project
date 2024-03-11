package account.presentationlayer;

import account.businesslayer.dto.UserDeletedDto;
import account.businesslayer.entity.User;
import account.businesslayer.dto.UserAdapter;
import account.businesslayer.UserService;
import account.businesslayer.dto.UpdateSuccessfulDto;
import account.businesslayer.dto.UserDto;
import account.businesslayer.request.PasswordChangeRequest;
import account.businesslayer.request.RoleChangeRequest;
import account.businesslayer.request.UserRegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping(path = "/api/auth/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserRegistrationRequest newUser) {
        userService.validateEmail(newUser.email());
        userService.validateNewPassword(newUser.password());
        User user = userService.register(newUser);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(userService.buildUserDto(user));
    }

    @GetMapping(path = "/api/admin/user/")
    public ResponseEntity<?> getUsers(@AuthenticationPrincipal UserAdapter user){
        UserDto[] allUsers = userService.handleGetUsers();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(allUsers);
    }

    @PutMapping(path = "/api/admin/user/role")
    public ResponseEntity<?> setRoles(@RequestBody RoleChangeRequest roleChangeRequest){
        UserDto updatedUser = userService.handleRoleChange(roleChangeRequest);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(updatedUser);
    }

    @DeleteMapping(path = "/api/admin/user/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email){
        UserDeletedDto response = userService.handleUserDelete(email);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    @PostMapping(path = "/api/auth/changepass")
    public ResponseEntity<?> changePass(
        @RequestBody PasswordChangeRequest newPassword, @AuthenticationPrincipal UserAdapter user) {

        userService.validatePasswordUpdate(newPassword.password(), user.getPassword());
        User updatedUser = userService.updatePassword(newPassword.password(), user);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new UpdateSuccessfulDto(
                updatedUser.getEmail(), "The password has been updated successfully")
            );
    }
}
