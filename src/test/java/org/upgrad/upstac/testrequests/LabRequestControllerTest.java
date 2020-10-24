package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.Contracts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.consultation.Consultation;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabRequestController;
import org.upgrad.upstac.testrequests.lab.LabResultService;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.users.User;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
class LabRequestControllerTest {


    @InjectMocks
    LabRequestController labRequestController;

    @Mock
    LabResultService labResultService;

    @Mock
    TestRequestQueryService testRequestQueryService;

    @Mock
    TestRequestUpdateService testRequestUpdateService;

    @Mock
    UserLoggedInService userLoggedInService;


    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_update_the_request_status(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.INITIATED);

        //Implement this method
        User user= createUser();


        //Create another object of the TestRequest method and explicitly assign this object for Lab Test using assignForLabTest() method
        // from labRequestController class. Pass the request id of testRequest object.
        TestRequest result = new TestRequest();

        //Use assertThat() methods to perform the following two comparisons
        //  1. the request ids of both the objects created should be same
        //  2. the status of the second object should be equal to 'INITIATED'
        // make use of assertNotNull() method to make sure that the lab result of second object is not null
        // use getLabResult() method to get the lab result
        assertNotNull(result);
        assertEquals(result.getRequestId(), testRequest.getRequestId());
        assertEquals(RequestStatus.INITIATED, result.getStatus());

    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        TestRequest result = testRequestQueryService.findBy(status).stream().findFirst().orElse(null);
        if (result==null){
            result = new TestRequest();
            result.setStatus(status);
        }
        return  result;
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_throw_exception(){

        Long InvalidRequestId= -34L;
        User user= createUser();

        //Implement this method
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        Mockito.when(testRequestUpdateService.assignForLabTest(InvalidRequestId, user)).thenThrow(new AppException("Invalid ID"));

        // Create an object of ResponseStatusException . Use assertThrows() method and pass assignForLabTest() method
        // of labRequestController with InvalidRequestId as Id
        ResponseStatusException result = assertThrows(ResponseStatusException.class,()->{
            labRequestController.assignForLabTest(InvalidRequestId);
        });

        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "Invalid ID"
        assertNotNull(result);
        assertEquals(result.getReason(),"Invalid ID");
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_valid_test_request_id_should_update_the_request_status_and_update_test_request_details(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        User user = createUser();

        //Implement this method
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);

        //Create an object of CreateLabResult and call getCreateLabResult() to create the object. Pass the above created object as the parameter
        CreateLabResult createLabResult = getCreateLabResult(testRequest);

        //Create another object of the TestRequest method and explicitly update the status of this object
        // to be 'LAB_TEST_IN_PROGRESS'. Make use of updateLabTest() method from labRequestController class (Pass the previously created two objects as parameters)
        TestRequest mockedResponse = new TestRequest();
        mockedResponse.setStatus(RequestStatus.LAB_TEST_IN_PROGRESS);

        Mockito.when(testRequestUpdateService.updateLabTest(testRequest.getRequestId(), createLabResult, user)).thenReturn(mockedResponse);

        TestRequest result = labRequestController.updateLabTest(testRequest.getRequestId(), createLabResult);
        //Use assertThat() methods to perform the following three comparisons
        //  1. the request ids of both the objects created should be same
        //  2. the status of the second object should be equal to 'LAB_TEST_COMPLETED'
        // 3. the results of both the objects created should be same. Make use of getLabResult() method to get the results.

        assertNotNull(result);
        assertEquals(result.getRequestId(), mockedResponse.getRequestId());
        assertEquals(mockedResponse.getStatus(), RequestStatus.LAB_TEST_COMPLETED);

    }


    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_test_request_id_should_throw_exception(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        User user = createUser();
        Long InvalidRequestId= -34L;

        //Implement this method
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);

        //Create an object of CreateLabResult and call getCreateLabResult() to create the object. Pass the above created object as the parameter
        CreateLabResult createLabResult = getCreateLabResult(testRequest);

        Mockito.when(testRequestUpdateService.updateLabTest(InvalidRequestId, createLabResult, user)).thenThrow(new AppException("Invalid ID"));
        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateLabTest() method
        // of labRequestController with a negative long value as Id and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method
        ResponseStatusException result = assertThrows(ResponseStatusException.class,()->{
            labRequestController.updateLabTest(InvalidRequestId, createLabResult);
        });

        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "Invalid ID"
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertEquals("Invalid ID", result.getReason());

    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_empty_status_should_throw_exception(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        User user = createUser();

        //Implement this method
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);

        //Create an object of CreateLabResult and call getCreateLabResult() to create the object. Pass the above created object as the parameter
        // Set the result of the above created object to null.
        CreateLabResult createLabResult = getCreateLabResult(testRequest);
        createLabResult.setResult(null);

        Mockito.when(testRequestUpdateService.updateLabTest(testRequest.getRequestId(), createLabResult, user)).thenThrow(new AppException("ConstraintViolationException"));
        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateLabTest() method
        // of labRequestController with request Id of the testRequest object and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method
        ResponseStatusException result = assertThrows(ResponseStatusException.class,()->{
            labRequestController.updateLabTest(testRequest.getRequestId(), createLabResult);
        });

        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "ConstraintViolationException"
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertEquals("ConstraintViolationException", result.getReason());
    }

    public CreateLabResult getCreateLabResult(TestRequest testRequest) {

        //Create an object of CreateLabResult and set all the values
        // Return the object

        CreateLabResult createLabResult = new CreateLabResult();
        createLabResult.setBloodPressure("90");
        createLabResult.setComments("Ok");
        createLabResult.setHeartBeat("30");
        createLabResult.setOxygenLevel("100");
        createLabResult.setTemperature("80");
        createLabResult.setResult(TestStatus.NEGATIVE);

        return  createLabResult;

    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUserName("someuser");
        return user;
    }

}