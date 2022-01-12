package karstenroethig.springbootblueprint.webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.springbootblueprint.webapp.model.domain.User;

public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User>
{
	Optional<User> findOneByEmailIgnoreCase(String email);
}
