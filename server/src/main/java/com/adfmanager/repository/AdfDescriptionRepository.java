package com.adfmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.adfmanager.domain.AdfDescription;


public interface AdfDescriptionRepository extends JpaRepository<AdfDescription, Long> {
}