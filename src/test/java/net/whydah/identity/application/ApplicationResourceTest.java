package net.whydah.identity.application;

import net.whydah.identity.audit.AuditLogRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="bard.lind@gmail.com">Bard Lind</a>
 */
public class ApplicationResourceTest {
    private static final Logger log = LoggerFactory.getLogger(ApplicationResourceTest.class);
    ApplicationRepository applicationRepositoryMock;
    AuditLogRepository auditLogRepositoryMock;
    ApplicationService applicationService;
    ApplicationResource applicationResource;
    ApplicationsResource applicationsResource;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @Before
    public void setUp() throws Exception {
        applicationRepositoryMock = mock(ApplicationRepository.class);
        auditLogRepositoryMock = mock(AuditLogRepository.class);

        applicationService = new ApplicationService(applicationRepositoryMock, auditLogRepositoryMock);
        applicationResource = new ApplicationResource(applicationService);
        applicationsResource = new ApplicationsResource(applicationService);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCreateApplication() throws Exception {
        applicationResource.createApplication(allApplication);
    }

    @Test
    public void testGetApplications() throws Exception {

        applicationResource.createApplication(allApplication);
        applicationResource.createApplication(application2);
        Response res = applicationsResource.getApplications();
        res.getStatus();
    }


    @Test
    @Ignore
    public void testCreateApplicationFails() throws Exception {
        try {
            Response res = applicationResource.createApplication("malformedjson");
            System.out.println(res.getStatus());
            fail("Creation of non-valid application allowed");
        } catch (IllegalArgumentException iae) {

        } catch (Exception jpe){

        }
    }
    private final String allApplication = "{\"id\":\"id1\",\"name\":\"test\",\"defaultRoleName\":\"default1role\",\"defaultOrgName\":\"defaultorgid\",\"availableOrgNames\":[\"developer@customer\",\"consultant@customer\"]}";
    private final String application2 = "{\"id\":\"id2\",\"name\":\"test2\",\"defaultRoleName\":\"default1role\",\"defaultOrgName\":\"defaultorgid\",\"availableOrgNames\":[\"developer@customer\",\"consultant@customer\"]}";
}

