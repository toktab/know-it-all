package com.knowitall.user.service;

import com.knowitall.user.dto.LeaderboardEntry;
import com.knowitall.user.dto.UpdateUserRequest;
import com.knowitall.user.dto.UserResponse;
import com.knowitall.user.entity.User;
import com.knowitall.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return toResponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        return toResponse(userRepository.save(user));
    }

    public void addScore(Long userId, int points) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTotalScore(user.getTotalScore() + points);
        user.setGamesPlayed(user.getGamesPlayed() + 1);
        userRepository.save(user);
    }

    public List<LeaderboardEntry> getLeaderboard() {
        List<User> users = userRepository.findAllByOrderByTotalScoreDesc();
        return java.util.stream.IntStream.range(0, users.size())
                .mapToObj(i -> LeaderboardEntry.builder()
                        .rank(i + 1)
                        .username(users.get(i).getUsername())
                        .totalScore(users.get(i).getTotalScore())
                        .gamesPlayed(users.get(i).getGamesPlayed())
                        .build())
                .toList();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .totalScore(user.getTotalScore())
                .gamesPlayed(user.getGamesPlayed())
                .createdAt(user.getCreatedAt())
                .build();
    }
}