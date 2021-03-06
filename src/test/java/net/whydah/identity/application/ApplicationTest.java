package net.whydah.identity.application;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class ApplicationTest {
    @Test
    public void testToJson() throws Exception {
        Application application = new Application("id1", "test");
        JSONAssert.assertEquals(minimumApplication, application.toJson(),false);
        application = new Application("id1", "test", "defaultrole", "defaultorgid");
        JSONAssert.assertEquals(mostApplication, application.toJson(), false);
        List<String> availableOrgNames = new ArrayList<>();
        availableOrgNames.add("developer@customer");
        availableOrgNames.add("consultant@customer");
        application.setAvailableOrgNames(availableOrgNames);
        JSONAssert.assertEquals(allApplication, application.toJson(), false);
    }

    private final String minimumApplication = "{\"id\":\"id1\",\"name\":\"test\",\"defaultRoleName\":null,\"defaultOrgName\":null,\"availableOrgNames\":null}";
    private final String mostApplication = "{\"id\":\"id1\",\"name\":\"test\",\"defaultRoleName\":\"defaultrole\",\"defaultOrgName\":\"defaultorgid\",\"availableOrgNames\":null}";
    private final String allApplication = "{\"id\":\"id1\",\"name\":\"test\",\"defaultRoleName\":\"defaultrole\",\"defaultOrgName\":\"defaultorgid\",\"availableOrgNames\":[\"developer@customer\",\"consultant@customer\"]}";

}
