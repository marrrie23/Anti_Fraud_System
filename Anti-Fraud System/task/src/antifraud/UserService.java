package antifraud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public User registerUser(String name, String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("User already exists");
        }

        User newUser = new User();
        newUser.setName(name);
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));

        // Determine role based on whether there are other users
        if (userRepository.count() == 0) {
            newUser.setRole(Role.ADMINISTRATOR);
            newUser.setAccountNonLocked(true); // First user (ADMINISTRATOR) is unlocked
        } else {
            newUser.setRole(Role.MERCHANT);
            newUser.setAccountNonLocked(false); // All others are locked by default
        }

        return userRepository.save(newUser);
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public boolean deleteUser(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (user != null) {
            userRepository.delete(user);
            return true;
        }
        return false;
    }

    public User changeUserRole(String username, String role) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Role newRole = Role.valueOf(role);
        if (user.getRole().equals(newRole)) {
            throw new IllegalStateException("User already has the role: " + role);
        }

        if (newRole == Role.ADMINISTRATOR) {
            throw new IllegalArgumentException("Cannot assign ADMINISTRATOR role to existing users.");
        }

        user.setRole(newRole);
        return userRepository.save(user);
    }

    public void updateUserAccess(String username, String operation) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == Role.ADMINISTRATOR) {
            throw new IllegalStateException("Cannot lock/unlock ADMINISTRATOR");
        }

        boolean unlock = operation.equalsIgnoreCase("UNLOCK");
        user.setAccountNonLocked(unlock);
        userRepository.save(user);
    }
}
