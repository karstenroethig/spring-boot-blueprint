package karstenroethig.springbootblueprint.webapp.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import karstenroethig.springbootblueprint.webapp.model.domain.User;
import karstenroethig.springbootblueprint.webapp.model.domain.VerificationToken;

public interface VerificationTokenRepository extends CrudRepository<VerificationToken, Long>
{
	Optional<VerificationToken> findOneByToken(String token);

	Long deleteByUser(User user);
}