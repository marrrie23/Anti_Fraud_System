package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().isEmpty() ||
                userDto.getUsername() == null || userDto.getUsername().isEmpty() ||
                userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid data"));
        }

        try {
            User user = userService.registerUser(userDto.getName(), userDto.getUsername(), userDto.getPassword());
            UserDto responseDto = new UserDto(user.getId(), user.getName(), user.getUsername(), user.getRole().name());
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserDto>> listUsers() {
        List<UserDto> userDtos = userService.listUsers().stream()
                .map(user -> new UserDto(user.getId(), user.getName(), user.getUsername(), user.getRole().name()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @PutMapping("/role")
    public ResponseEntity<?> changeUserRole(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String role = request.get("role");

        if (!role.equals("SUPPORT") && !role.equals("MERCHANT")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
        }

        try {
            User user = userService.changeUserRole(username, role);
            UserDto responseDto = new UserDto(user.getId(), user.getName(), user.getUsername(), user.getRole().name());
            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/access")
    public ResponseEntity<?> updateUserAccess(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String operation = request.get("operation");

        if (operation == null || (!operation.equals("LOCK") && !operation.equals("UNLOCK"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid operation"));
        }

        try {
            userService.updateUserAccess(username, operation);
            return ResponseEntity.ok(Map.of("status", "User " + username + " " + operation.toLowerCase() + "ed!"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username) {
        boolean deleted = userService.deleteUser(username);
        if (deleted) {
            return ResponseEntity.ok(Map.of("username", username, "status", "Deleted successfully!"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
    }
}
