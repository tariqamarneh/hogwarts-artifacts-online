package edu.tcu.cs.hogwartsartifactsonline.hogwartsuser;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<HogwartsUser, Integer> {
}
