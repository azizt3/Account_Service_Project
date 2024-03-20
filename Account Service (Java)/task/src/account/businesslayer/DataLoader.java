package account.businesslayer;

import account.businesslayer.entity.Authority;
import account.persistencelayer.AuthorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    private AuthorityRepository authorityRepository;

    @Autowired
    public DataLoader (AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
        createAuthorities();
    }

    public void createAuthorities(){
        try{
            authorityRepository.save(new Authority("ROLE_ADMINISTRATOR", "ADMINISTRATIVE"));
            authorityRepository.save(new Authority("ROLE_USER", "BUSINESS_USER"));
            authorityRepository.save(new Authority("ROLE_ACCOUNTANT", "BUSINESS_USER"));
        } catch (Exception e) {

        }
    }

}
