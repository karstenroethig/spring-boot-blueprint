package karstenroethig.springbootblueprint.webapp.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import karstenroethig.springbootblueprint.webapp.model.domain.PasswordResetToken;
import karstenroethig.springbootblueprint.webapp.model.domain.User;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, Long>
{
	Optional<PasswordResetToken> findOneByToken(String token);

	Long deleteByUser(User user);
}