package net.whydah.identity.user.identity;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.whydah.identity.audit.ActionPerformed;
import net.whydah.identity.audit.AuditLogRepository;
import net.whydah.identity.user.ChangePasswordToken;
import net.whydah.identity.user.email.PasswordSender;
import net.whydah.identity.util.PasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * @author <a href="mailto:erik-dev@fjas.no">Erik Drolshammer</a> 29.03.14
 */
@Singleton
public class UserIdentityService {
    private static final Logger log = LoggerFactory.getLogger(UserIdentityService.class);

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm");
    private static final String SALT_ENCODING = "UTF-8";

    //@Inject @Named("internal") private LdapAuthenticatorImpl internalLdapAuthenticator;
    private final LdapAuthenticatorImpl externalLdapAuthenticator;
    private final LDAPHelper ldapHelper;
    private final AuditLogRepository auditLogRepository;

    private final PasswordGenerator passwordGenerator;
    private final PasswordSender passwordSender;


    @Inject
    public UserIdentityService(@Named("external") LdapAuthenticatorImpl externalLdapAuthenticator,
                               LDAPHelper ldapHelper, AuditLogRepository auditLogRepository, PasswordGenerator passwordGenerator,
                               PasswordSender passwordSender) {
        this.externalLdapAuthenticator = externalLdapAuthenticator;
        this.ldapHelper = ldapHelper;
        this.auditLogRepository = auditLogRepository;
        this.passwordGenerator = passwordGenerator;
        this.passwordSender = passwordSender;
    }

    public UserIdentity authenticate(final String username, final String password) {
        return externalLdapAuthenticator.authenticate(username, password);
    }


    public void resetPassword(String username, String uid, String userEmail) {
        String token = setTempPassword(username, uid);
        passwordSender.sendResetPasswordEmail(username, token, userEmail);
    }
    private String setTempPassword(String username, String uid) {
        String newPassword = passwordGenerator.generate();
        String salt = passwordGenerator.generate();
        ldapHelper.setTempPassword(username, newPassword, salt);
        audit(ActionPerformed.MODIFIED, "resetpassword", uid);

        byte[] saltAsBytes;
        try {
            saltAsBytes = salt.getBytes(SALT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        ChangePasswordToken changePasswordToken = new ChangePasswordToken(username, newPassword);
        return changePasswordToken.generateTokenString(saltAsBytes);
    }

    /**
     * Authenticate using token generated when resetting the password
     * @param username  username to authenticate
     * @param token with temporary access
     * @return  true if authentication OK
     */
    public boolean authenticateWithChangePasswordToken(String username, String token) {
        String salt = ldapHelper.getSalt(username);

        byte[] saltAsBytes;
        try {
            saltAsBytes = salt.getBytes(SALT_ENCODING);
        } catch (UnsupportedEncodingException e1) {
            throw new RuntimeException("Error with salt for username=" + username, e1);
        }
        ChangePasswordToken changePasswordToken = ChangePasswordToken.fromTokenString(token, saltAsBytes);
        boolean ok = externalLdapAuthenticator.authenticateWithTemporaryPassword(username, changePasswordToken.getPassword());
        log.info("authenticateWithChangePasswordToken was ok={} for username={}", username, ok);
        return ok;
    }


    public void changePassword(String username, String userUid, String newPassword) {
        ldapHelper.changePassword(username, newPassword);
        audit(ActionPerformed.MODIFIED, "password", userUid);
    }

    public void addUserToLdap(UserIdentity userIdentity) {
        String username = userIdentity.getUsername();
        try {
            if (ldapHelper.usernameExist(username)) {
                //return Response.status(Response.Status.NOT_ACCEPTABLE).build();
                throw new IllegalStateException("User already exists, could not create user " + username);
            }
        } catch (NamingException e) {
            throw new RuntimeException("usernameExist failed for username=" + username, e);
        }

        userIdentity.setPassword(passwordGenerator.generate());
        userIdentity.setUid(UUID.randomUUID().toString());

        try {
            ldapHelper.addWhydahUserIdentity(userIdentity);
        } catch (NamingException e) {
            throw new RuntimeException("addWhydahUserIdentity failed for " + userIdentity.toString(), e);
        }
        log.info("Added new user to LDAP: {}", username);
    }

    public UserIdentity getUserIndentity(String username) throws NamingException {
        return ldapHelper.getUserIndentity(username);
    }

    public void updateUser(String username, UserIdentity newuser) {
        ldapHelper.updateUser(username, newuser);
    }

    public void deleteUser(String username) {
        ldapHelper.deleteUser(username);
    }

    private void audit(String action, String what, String value) {
        String now = sdf.format(new Date());
        ActionPerformed actionPerformed = new ActionPerformed(value, now, action, what, value);
        auditLogRepository.store(actionPerformed);
    }
}
