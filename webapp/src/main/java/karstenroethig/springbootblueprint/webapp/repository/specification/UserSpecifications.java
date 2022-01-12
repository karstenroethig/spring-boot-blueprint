package karstenroethig.springbootblueprint.webapp.repository.specification;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import karstenroethig.springbootblueprint.webapp.model.domain.User;
import karstenroethig.springbootblueprint.webapp.model.domain.User_;
import karstenroethig.springbootblueprint.webapp.model.dto.search.UserSearchDto;
import karstenroethig.springbootblueprint.webapp.model.dto.search.UserSearchDto.EnabledSearchTypeEnum;
import karstenroethig.springbootblueprint.webapp.model.dto.search.UserSearchDto.LockedSearchTypeEnum;

public class UserSpecifications
{
	private UserSpecifications() {}

	public static Specification<User> matchesSearchParam(UserSearchDto userSearchDto)
	{
		return (Root<User> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				addRestrictionsForName(root, cb, restrictions, userSearchDto.getName());
				addRestrictionsForEnabled(root, cb, restrictions, userSearchDto.getEnabledSearchType());
				addRestrictionsForLocked(root, cb, restrictions, userSearchDto.getLockedSearchType());
				addRestrictionsForDeleted(root, cb, restrictions);

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}

	private static void addRestrictionsForName(Root<User> root, CriteriaBuilder cb, List<Predicate> restrictions, String name)
	{
		if (StringUtils.isBlank(name))
			return;

		restrictions.add(cb.or(
				cb.like(cb.lower(root.get(User_.email)), "%" + StringUtils.lowerCase(name) + "%"),
				cb.like(cb.lower(root.get(User_.fullName)), "%" + StringUtils.lowerCase(name) + "%")));
	}

	private static void addRestrictionsForEnabled(Root<User> root, CriteriaBuilder cb, List<Predicate> restrictions, EnabledSearchTypeEnum enabledSearchType)
	{
		if (enabledSearchType == null)
			return;

		if (enabledSearchType == EnabledSearchTypeEnum.ENABLED)
			restrictions.add(cb.equal(root.get(User_.enabled), Boolean.TRUE));
		else if (enabledSearchType == EnabledSearchTypeEnum.DISABLED)
			restrictions.add(cb.equal(root.get(User_.enabled), Boolean.FALSE));
		else
			throw new IllegalArgumentException("unknown enabled search type " + enabledSearchType.name());
	}

	private static void addRestrictionsForLocked(Root<User> root, CriteriaBuilder cb, List<Predicate> restrictions, LockedSearchTypeEnum lockedSearchType)
	{
		if (lockedSearchType == null)
			return;

		if (lockedSearchType == LockedSearchTypeEnum.LOCKED)
			restrictions.add(cb.equal(root.get(User_.locked), Boolean.TRUE));
		else if (lockedSearchType == LockedSearchTypeEnum.UNLOCKED)
			restrictions.add(cb.equal(root.get(User_.locked), Boolean.FALSE));
		else
			throw new IllegalArgumentException("unknown locked search type " + lockedSearchType.name());
	}

	private static void addRestrictionsForDeleted(Root<User> root, CriteriaBuilder cb, List<Predicate> restrictions)
	{
		restrictions.add(cb.equal(root.get(User_.deleted), Boolean.FALSE));
	}
}
