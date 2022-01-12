package karstenroethig.springbootblueprint.webapp.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import karstenroethig.springbootblueprint.webapp.model.domain.Authority;

public interface AuthorityRepository extends CrudRepository<Authority,Long>
{
	Optional<Authority> findOneByNameIgnoreCase(String name);
}
