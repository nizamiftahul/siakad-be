package com.siakad.auth.repository;

import com.siakad.auth.entity.UserEntity;
import com.siakad.common.enums.Jenjang;
import com.siakad.common.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Integer id);

    @Query("""
        SELECT u FROM UserEntity u
        WHERE u.id = :id
          AND u.jenjang = :jenjang
          AND u.role IN :roles
        """)
    Optional<UserEntity> findByIdAndJenjangAndRoleIn(@Param("id") Integer id,
        @Param("jenjang") Jenjang jenjang,
        @Param("roles") List<Role> roles);

    @Query("""
        SELECT u FROM UserEntity u
        WHERE u.jenjang = :jenjang
          AND u.role IN :roles
          AND (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%')))
          AND (CAST(:role AS string) IS NULL OR u.role = :role)
        ORDER BY u.name
        """)
    Page<UserEntity> search(@Param("jenjang") Jenjang jenjang,
        @Param("roles") List<Role> roles,
        @Param("name") String name,
        @Param("role") Role role,
        Pageable pageable);
}
