package com.nihal.Music.repositories;

import com.nihal.Music.entity.User;
import com.nihal.Music.entity.role.AuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String email);

    Optional<User> findByProviderIdAndProviderType(String providerId, AuthProviderType providerType);

}