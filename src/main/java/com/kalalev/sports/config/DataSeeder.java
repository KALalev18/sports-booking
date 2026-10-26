package com.kalalev.sports.config;

import com.kalalev.sports.model.Field;
import com.kalalev.sports.model.User;
import com.kalalev.sports.model.UserRole;
import com.kalalev.sports.repository.FieldRepository;
import com.kalalev.sports.repository.UserRepository;
import com.kalalev.sports.repository.UserRoleRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fills an empty database with the two roles, a demo admin and user, and the facilities
 * from the original version of the site. Each part only runs when its table is empty, so restarting
 * against a real database does not create duplicates.
 */
@Component
public class DataSeeder implements CommandLineRunner {
    private final UserRoleRepository roleRepository;
    private final UserRepository userRepository;
    private final FieldRepository fieldRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String userEmail;
    private final String userPassword;

    public DataSeeder(
            UserRoleRepository roleRepository,
            UserRepository userRepository,
            FieldRepository fieldRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.demo.admin-email}") String adminEmail,
            @Value("${app.demo.admin-password}") String adminPassword,
            @Value("${app.demo.user-email}") String userEmail,
            @Value("${app.demo.user-password}") String userPassword) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.fieldRepository = fieldRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.userEmail = userEmail;
        this.userPassword = userPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(new UserRole(UserRole.USER), new UserRole(UserRole.ADMIN)));
        }
        if (userRepository.count() == 0) {
            UserRole admin = roleRepository.findByRoleName(UserRole.ADMIN).orElseThrow();
            UserRole user = roleRepository.findByRoleName(UserRole.USER).orElseThrow();
            userRepository.save(new User("Demo", "Admin", adminEmail, passwordEncoder.encode(adminPassword), admin));
            userRepository.save(new User("Demo", "User", userEmail, passwordEncoder.encode(userPassword), user));
        }
        if (fieldRepository.count() == 0) {
            fieldRepository.saveAll(fields());
        }
    }

    private static List<Field> fields() {
        return List.of(
                new Field(
                        "National Sports Base",
                        "Sofia",
                        "Monday to Friday",
                        LocalTime.of(9, 0),
                        LocalTime.of(20, 0),
                        new BigDecimal("50.00"),
                        "https://www.nsb.bg/wp-content/uploads/2019/04/IMG_4008.jpg",
                        "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3315.4817122461527!2d23.33089556952405!3d42.69296100957003!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x40aa85a005596f8f%3A0xaf0e9ec4431f0d88!2z0J3QsNGG0LjQvtC90LDQu9C90LAg0YHQv9C-0YDRgtC90LAg0LHQsNC30LA!5e0!3m2!1sen!2sbg!4v1680636253739!5m2!1sen!2sbg"),
                new Field(
                        "Ivan Vazov School Sports Ground",
                        "Pomorie",
                        "Monday to Friday",
                        LocalTime.of(8, 0),
                        LocalTime.of(21, 0),
                        new BigDecimal("20.00"),
                        "https://www.zonaburgas.bg/uploads/2018/01/20003-Large.jpg",
                        "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d1469.383239021384!2d27.63755949221781!3d42.56025123293111!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x40a6bd634ecffa4d%3A0xe61c36e43242f3f8!2z0KHQntCjICLQmNCy0LDQvSDQktCw0LfQvtCyIiAtINC80LDQu9C60L7RgtC-INGD0YfQuNC70LjRidC1!5e0!3m2!1sen!2sbg!4v1679988221592!5m2!1sen!2sbg"),
                new Field(
                        "Mladost Indoor Sports Hall",
                        "Burgas",
                        "Monday to Sunday",
                        LocalTime.of(9, 0),
                        LocalTime.of(18, 0),
                        new BigDecimal("50.00"),
                        "https://imgrabo.com/pics/guide/900x600/20200116133716_35460.jpg",
                        "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d11762.90871073404!2d27.45701919050962!3d42.51860542229421!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x40a69379293728c3%3A0x74cc273bccbe3f72!2z0KHQv9C-0YDRgtC90LAg0LfQsNC70LAgItCc0LvQsNC00L7RgdGCIiwg0LPRgNCw0LQg0JHRg9GA0LPQsNGB!5e0!3m2!1sen!2sbg!4v1680635981678!5m2!1sen!2sbg"));
    }
}
