package net.whydah.identity.user.search;

import com.google.inject.Inject;
import net.whydah.identity.user.identity.LdapUserIdentityDao;
import net.whydah.identity.user.identity.UserIdentity;
import net.whydah.identity.user.identity.UserIdentityRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.util.List;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class UserSearch {
    private static final Logger log = LoggerFactory.getLogger(UserSearch.class);
    private final LdapUserIdentityDao ldapUserIdentityDao;
    private final LuceneSearch luceneSearch;

    @Inject
    public UserSearch(LdapUserIdentityDao ldapUserIdentityDao, LuceneSearch luceneSearch) {
        this.ldapUserIdentityDao = ldapUserIdentityDao;
        this.luceneSearch = luceneSearch;
    }

    public List<UserIdentityRepresentation> search(String query) {
        List<UserIdentityRepresentation> users = luceneSearch.search(query);
        //If user is not found in lucene, try to search AD.
        if (users == null || users.size() <1) {
            try {
                UserIdentity user = ldapUserIdentityDao.getUserIndentity(query);
                if (user != null) {
                    users.add(user);
                }
            } catch (NamingException e) {
                log.warn("Could not find users from ldap/AD. Query: {}", query, e);
            }
        }
        return users;
    }
}
