package com.avodev.techstore.repositories;

import com.avodev.techstore.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    void deleteByName(String name);
    List<Permission>  findAllByNameIn(List<String> names);
    boolean existsByName(String name);
}
