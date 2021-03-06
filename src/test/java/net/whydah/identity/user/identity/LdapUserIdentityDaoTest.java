package net.whydah.identity.user.identity;

import net.whydah.identity.config.AppConfig;
import net.whydah.identity.util.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.*;

public class LdapUserIdentityDaoTest {
    private static EmbeddedADS ads;
    private static LdapUserIdentityDao ldapUserIdentityDao;
    private static LdapAuthenticator ldapAuthenticator;

    @BeforeClass
    public static void setUp() throws Exception {
        System.setProperty(AppConfig.IAM_MODE_KEY, AppConfig.IAM_MODE_DEV);

        //int LDAP_PORT = new Integer(AppConfig.appConfig.getProperty("ldap.embedded.port"));
        int LDAP_PORT = 18389;
        String ldapUrl = "ldap://localhost:" + LDAP_PORT + "/dc=external,dc=WHYDAH,dc=no";
        boolean readOnly = Boolean.parseBoolean(AppConfig.appConfig.getProperty("ldap.primary.readonly"));
        ldapUserIdentityDao = new LdapUserIdentityDao(ldapUrl, "uid=admin,ou=system", "secret", "uid", "initials", readOnly);
        ldapAuthenticator = new LdapAuthenticator(ldapUrl, "uid=admin,ou=system", "secret", "uid", "initials");

        String workDirPath = "target/" + LdapUserIdentityDaoTest.class.getSimpleName();
        File workDir = new File(workDirPath);
        FileUtils.deleteDirectory(workDir);
        if (!workDir.mkdirs()) {
            fail("Error creating working directory " + workDirPath);
        }
        // Create the server
        ads = new EmbeddedADS(workDir);
        ads.startServer(LDAP_PORT);
        Thread.sleep(1000);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (ads != null) {
            ads.stopServer();
        }
    }

    @Test
    public void testAddUser() throws Exception {
        String uid = "staven@hotmail.com";
        String username = "jan";
        String firstName = "Oddvar";
        String lastName = "jensen";
        String email = "staven@hotmail.com";
        String password = "pass";
        String cellPhone = "+4798765432";
        String personRef = "some@email.dk";
        UserIdentity user = new UserIdentity(uid, username, firstName, lastName, email, password, cellPhone, personRef);
        ldapUserIdentityDao.addUserIdentity(user);
        UserIdentity gotUser = ldapUserIdentityDao.getUserIndentity("jan");
        assertNotNull(gotUser);
        assertEquals(gotUser.getUid(), uid);
        assertEquals(gotUser.getUsername(), username);
        assertEquals(gotUser.getFirstName(), firstName);
        assertEquals(gotUser.getLastName(), lastName);
        assertEquals(gotUser.getEmail(), email);
        //assertEquals(gotUser.getPassword(), password);
        assertEquals(gotUser.getCellPhone(), cellPhone);
        assertEquals(gotUser.getPersonRef(), personRef);
    }

    @Test
    public void testUpdateUser() throws Exception {
        String uid = UUID.randomUUID().toString();
        String username = "nalle";
        UserIdentity user = createValidUser(uid, username, "Nalle", "Puh", "nalle@hotmail.com", "pass");
        ldapUserIdentityDao.addUserIdentity(user);
        UserIdentity gotUser = ldapUserIdentityDao.getUserIndentity(username);
        assertNull(gotUser.getCellPhone());

        String cellPhone = "32323232";
        String personRef = "abc/123";
        gotUser.setCellPhone(cellPhone);
        gotUser.setPersonRef(personRef);
        ldapUserIdentityDao.updateUserIdentityForUsername(username, gotUser);
        UserIdentity gotUpdatedUser = ldapUserIdentityDao.getUserIndentity(username);
        assertEquals(cellPhone, gotUpdatedUser.getCellPhone());
        assertEquals(personRef, gotUpdatedUser.getPersonRef());

        gotUpdatedUser.setCellPhone(null);
        String firstName = "Emil";
        personRef = "some@email.com";
        gotUpdatedUser.setFirstName(firstName);
        gotUpdatedUser.setPersonRef(personRef);
        ldapUserIdentityDao.updateUserIdentityForUsername(username, gotUpdatedUser);
        gotUpdatedUser = ldapUserIdentityDao.getUserIndentity(username);
        assertEquals(firstName, gotUpdatedUser.getFirstName());
        assertNull(gotUpdatedUser.getCellPhone());
        assertEquals(personRef, gotUpdatedUser.getPersonRef());
    }

    @Test
    public void testDeleteUser() throws Exception {
        String uid = UUID.randomUUID().toString();
        String username = "usernameToBeDeleted";
        UserIdentity user = createValidUser(uid, username, "Trevor", "Treske", "tretre@hotmail.com", "pass");
        ldapUserIdentityDao.addUserIdentity(user);
        UserIdentityRepresentation gotUser = ldapUserIdentityDao.getUserIndentity(user.getUsername());
        assertNotNull(gotUser);

        boolean deleteSuccessful = ldapUserIdentityDao.deleteUserIdentity(username);
        assertTrue(deleteSuccessful);

        UserIdentityRepresentation gotUser2 = ldapUserIdentityDao.getUserIndentity(username);
        assertNull("Expected user to be deleted. " + (gotUser2 != null ? gotUser2.toString() : "null"), gotUser2);
    }

    @Test
    public void testChangePassword() throws Exception {
        String username = "stoven@hotmail.com";
        String firstPassword = "pass";
        String uid = username;
        UserIdentity user = createValidUser(uid, username, "Oddvar", "Bra", "stoven@hotmail.com", firstPassword);
        ldapUserIdentityDao.addUserIdentity(user);

        assertNotNull(ldapAuthenticator.authenticateWithTemporaryPassword(username, firstPassword));
        String secondPassword = "snafs";
        assertNull(ldapAuthenticator.authenticate(username, secondPassword));

        ldapUserIdentityDao.changePassword(username, secondPassword);
        assertNull(ldapAuthenticator.authenticate(username, firstPassword));
        assertNotNull(ldapAuthenticator.authenticate(username, secondPassword));
    }

    private static UserIdentity createValidUser(String uid, String username, String firstName, String lastName, String email, String password) {
        return new UserIdentity(uid, username, firstName, lastName, email, password, null, null);
    }
}