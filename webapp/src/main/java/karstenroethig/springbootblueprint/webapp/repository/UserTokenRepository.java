package karstenroethig.springbootblueprint.webapp.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import karstenroethig.springbootblueprint.webapp.model.domain.User;
import karstenroethig.springbootblueprint.webapp.model.domain.UserToken;
import karstenroethig.springbootblueprint.webapp.model.enums.UserTokenTypeEnum;

public interface UserTokenRepository extends CrudRepository<UserToken, Long>
{
	Optional<UserToken> findOneByTokenAndType(String token, UserTokenTypeEnum type);

	Long deleteByUserAndType(User user, UserTokenTypeEnum type);
}