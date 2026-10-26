package com.kalalev.booking.config;

import com.kalalev.booking.domain.AppUser;
import com.kalalev.booking.domain.Booking;
import com.kalalev.booking.domain.Court;
import com.kalalev.booking.domain.Facility;
import com.kalalev.booking.domain.Role;
import com.kalalev.booking.domain.Sport;
import com.kalalev.booking.domain.TimeSlot;
import com.kalalev.booking.repository.AppUserRepository;
import com.kalalev.booking.repository.BookingRepository;
import com.kalalev.booking.repository.CourtRepository;
import com.kalalev.booking.repository.FacilityRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fills an empty database with demo facilities, accounts and two weeks of bookings, so the live demo has something
 * to look at. Only runs with the "demo" profile and only when there are no users yet. The first three facilities
 * are the ones from the original 2023 version of the project.
 */
@Component
@Profile("demo")
public class DemoDataSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final ZoneId SOFIA = ZoneId.of("Europe/Sofia");

    private final AppUserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final CourtRepository courtRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;
    private final String demoPassword;

    public DemoDataSeeder(
            AppUserRepository userRepository,
            FacilityRepository facilityRepository,
            CourtRepository courtRepository,
            BookingRepository bookingRepository,
            PasswordEncoder passwordEncoder,
            Clock clock,
            @Value("${booking.demo.password:demo1234}") String demoPassword) {
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;
        this.courtRepository = courtRepository;
        this.bookingRepository = bookingRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        Instant now = clock.instant();
        String hash = passwordEncoder.encode(demoPassword);
        userRepository.save(new AppUser("customer@example.com", hash, "Demo Customer", Role.CUSTOMER, now));
        AppUser manager =
                userRepository.save(new AppUser("manager@example.com", hash, "Demo Manager", Role.MANAGER, now));
        userRepository.save(new AppUser("admin@example.com", hash, "Demo Admin", Role.ADMIN, now));
        List<AppUser> players = List.of(
                userRepository.save(new AppUser("maria@example.com", hash, "Maria Petrova", Role.CUSTOMER, now)),
                userRepository.save(new AppUser("georgi@example.com", hash, "Georgi Ivanov", Role.CUSTOMER, now)),
                userRepository.save(new AppUser("elena@example.com", hash, "Elena Dimitrova", Role.CUSTOMER, now)));

        Facility nsb = facility("National Sports Base", "Sofia", "Bul. Evlogi i Hristo Georgievi 38", 8, 22, manager);
        Facility pomorie = facility("Ivan Vazov School Sports Ground", "Pomorie", "Ul. Knyaz Boris I 2", 8, 21, null);
        Facility mladost = facility("Mladost Indoor Sports Hall", "Burgas", "Zh.k. Mladost 30", 9, 22, null);
        Facility seaGarden = facility("Sea Garden Courts", "Varna", "Primorski Park", 7, 23, manager);

        List<Court> courts = courtRepository.saveAll(List.of(
                new Court(nsb, "Hall A", Sport.BASKETBALL, eur("40")),
                new Court(nsb, "Hall B", Sport.VOLLEYBALL, eur("40")),
                new Court(nsb, "Tennis 1", Sport.TENNIS, eur("25")),
                new Court(nsb, "Tennis 2", Sport.TENNIS, eur("25")),
                new Court(pomorie, "Football pitch", Sport.FOOTBALL, eur("30")),
                new Court(pomorie, "Outdoor court", Sport.BASKETBALL, eur("15")),
                new Court(mladost, "Main hall", Sport.VOLLEYBALL, eur("35")),
                new Court(mladost, "Side hall", Sport.BASKETBALL, eur("30")),
                new Court(seaGarden, "Padel 1", Sport.PADEL, eur("28")),
                new Court(seaGarden, "Padel 2", Sport.PADEL, eur("28")),
                new Court(seaGarden, "Clay 1", Sport.TENNIS, eur("22"))));

        int bookings = seedBookings(courts, players, now);
        log.info("Demo data created: {} facilities, {} courts, {} bookings", 4, courts.size(), bookings);
    }

    /** Roughly one hour in three booked over the next 14 days, with a fixed seed so the demo looks the same. */
    private int seedBookings(List<Court> courts, List<AppUser> players, Instant now) {
        Random random = new Random(2023);
        LocalDate today = LocalDate.now(clock.withZone(SOFIA));
        List<Booking> bookings = new ArrayList<>();
        for (int day = 0; day < 14; day++) {
            LocalDate date = today.plusDays(day);
            for (Court court : courts) {
                Facility facility = court.getFacility();
                LocalTime start = facility.getOpensAt();
                while (start.isBefore(facility.getClosesAt())) {
                    int hours = 1 + random.nextInt(2);
                    LocalTime end = start.plusHours(hours);
                    if (end.isAfter(facility.getClosesAt()) || !end.isAfter(start)) {
                        break;
                    }
                    if (random.nextInt(3) == 0) {
                        TimeSlot slot = new TimeSlot(
                                date.atTime(start).atZone(SOFIA).toInstant(),
                                date.atTime(end).atZone(SOFIA).toInstant());
                        if (slot.start().isAfter(now)) {
                            AppUser player = players.get(random.nextInt(players.size()));
                            BigDecimal price = court.getPricePerHour().multiply(BigDecimal.valueOf(hours));
                            bookings.add(Booking.confirmed(court, player, slot, price, now));
                        }
                    }
                    start = end;
                }
            }
        }
        bookingRepository.saveAll(bookings);
        return bookings.size();
    }

    private Facility facility(String name, String city, String address, int opens, int closes, AppUser manager) {
        return facilityRepository.save(
                new Facility(name, city, address, SOFIA, LocalTime.of(opens, 0), LocalTime.of(closes, 0), manager));
    }

    private static BigDecimal eur(String amount) {
        return new BigDecimal(amount);
    }
}
