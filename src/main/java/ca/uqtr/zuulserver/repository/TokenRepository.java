package ca.uqtr.zuulserver.repository;

import ca.uqtr.zuulserver.entity.Token;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TokenRepository extends CrudRepository<Token, String> {
}
