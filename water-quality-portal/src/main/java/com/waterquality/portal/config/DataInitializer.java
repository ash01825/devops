package com.waterquality.portal.config;

import com.waterquality.portal.domain.Role;
import com.waterquality.portal.domain.Sample;
import com.waterquality.portal.domain.SampleStatus;
import com.waterquality.portal.domain.Station;
import com.waterquality.portal.domain.StatusHistory;
import com.waterquality.portal.domain.User;
import com.waterquality.portal.repository.SampleRepository;
import com.waterquality.portal.repository.StationRepository;
import com.waterquality.portal.repository.StatusHistoryRepository;
import com.waterquality.portal.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Seeds demo users, stations and samples on first start.
 * Idempotent: does nothing when users already exist.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository,
                               StationRepository stationRepository,
                               SampleRepository sampleRepository,
                               StatusHistoryRepository historyRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            userRepository.save(createUser("admin", "admin123", "System Admin",
                    "admin@waterquality.local", Role.ADMIN, passwordEncoder));
            userRepository.save(createUser("collector", "collector123", "Field Collector",
                    "collector@waterquality.local", Role.COLLECTOR, passwordEncoder));
            userRepository.save(createUser("analyst", "analyst123", "Lab Analyst",
                    "analyst@waterquality.local", Role.ANALYST, passwordEncoder));
            userRepository.save(createUser("reviewer", "reviewer123", "Quality Reviewer",
                    "reviewer@waterquality.local", Role.REVIEWER, passwordEncoder));

            Station s1 = station("ST-01", "River Main Intake", "Main River", 12.3456, 77.6543);
            Station s2 = station("ST-02", "North Reservoir", "North Lake", 12.9876, 77.1234);
            Station s3 = station("ST-03", "South Treatment Plant", "Canal South", 12.1111, 77.2222);
            Station s4 = station("ST-04", "East Borewell Cluster", "Groundwater East", 12.5555, 77.8888);
            stationRepository.save(s1);
            stationRepository.save(s2);
            stationRepository.save(s3);
            stationRepository.save(s4);

            seed(sampleRepository, historyRepository, "WQ-0001", s1, 6, "Field Collector",
                    "7.2", "24.5", "8.1", "2.3", "450", SampleStatus.DRAFT, "Seeded draft sample");
            seed(sampleRepository, historyRepository, "WQ-0002", s1, 5, "Field Collector",
                    "7.0", "25.0", "7.9", "3.1", "470", SampleStatus.SUBMITTED, "Submitted for analysis");
            seed(sampleRepository, historyRepository, "WQ-0003", s2, 4, "Priya Nair",
                    "6.9", "23.8", "8.4", "1.8", "430", SampleStatus.UNDER_REVIEW, "Under reviewer check");
            seed(sampleRepository, historyRepository, "WQ-0004", s2, 3, "Ravi Kumar",
                    "7.4", "26.1", "7.5", "4.0", "510", SampleStatus.APPROVED, "Approved batch");
            seed(sampleRepository, historyRepository, "WQ-0005", s3, 2, "Field Collector",
                    "8.1", "27.0", "6.8", "5.2", "560", SampleStatus.REJECTED, "High turbidity — recollect");
            seed(sampleRepository, historyRepository, "WQ-0006", s4, 1, "Anita Rao",
                    "7.1", "24.0", "8.0", "2.0", "445", SampleStatus.DRAFT, "Fresh field entry");
        };
    }

    private Station station(String code, String name, String waterBody, double lat, double lon) {
        Station s = new Station();
        s.setCode(code);
        s.setName(name);
        s.setWaterBody(waterBody);
        s.setLatitude(lat);
        s.setLongitude(lon);
        return s;
    }

    private void seed(SampleRepository samples, StatusHistoryRepository history,
                      String sampleId, Station station, int daysAgo, String collector,
                      String ph, String temp, String dox, String turb, String cond,
                      SampleStatus status, String notes) {
        Sample s = new Sample();
        s.setSampleId(sampleId);
        s.setStation(station);
        s.setCollectedAt(LocalDateTime.now().minusDays(daysAgo));
        s.setCollector(collector);
        s.setPh(new BigDecimal(ph));
        s.setTemperature(new BigDecimal(temp));
        s.setDissolvedOxygen(new BigDecimal(dox));
        s.setTurbidity(new BigDecimal(turb));
        s.setConductivity(new BigDecimal(cond));
        s.setStatus(status);
        s.setNotes(notes);
        samples.save(s);

        StatusHistory h = new StatusHistory();
        h.setSample(s);
        h.setFromStatus(null);
        h.setToStatus(status);
        h.setChangedBy("system");
        h.setChangedAt(LocalDateTime.now().minusDays(daysAgo));
        h.setComment("Seeded as " + status);
        history.save(h);
    }

    private User createUser(String username, String rawPassword, String fullName,
                            String email, Role role, PasswordEncoder encoder) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }
}
