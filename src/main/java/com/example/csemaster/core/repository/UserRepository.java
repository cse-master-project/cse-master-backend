package com.example.csemaster.core.repository;

import com.example.csemaster.core.dao.actor.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface UserRepository extends JpaRepository<UserEntity, String> {
}