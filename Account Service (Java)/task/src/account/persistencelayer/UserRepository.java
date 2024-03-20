package account.persistencelayer;

import account.businesslayer.entity.User;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
  boolean existsByEmail(String email);
  void deleteByEmail(String email);
  Optional<User> findByEmail(String email);
  List<User> findAll();

}
