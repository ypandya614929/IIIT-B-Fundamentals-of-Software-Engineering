package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.util.Contracts;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.parameters.P;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.consultation.ConsultationController;
import org.upgrad.upstac.testrequests.consultation.CreateConsultationRequest;
import org.upgrad.upstac.testrequests.consultation.DoctorSuggestion;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabRequestController;
import org.upgrad.upstac.testrequests.lab.LabResult;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.users.User;
import org.upgrad.upstac.users.models.Gender;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Slf4j
class ConsultationControllerTest {


    @InjectMocks
    ConsultationController consultationController;

    @Autowired
    TestRequestQueryService testRequestQueryService;

    @Mock
    TestRequestUpdateService testRequestUpdateService;

    @Mock
    UserLoggedInService userLoggedInService;

    @Mock
    TestRequestRepository testRequestRepository;

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_update_the_request_status(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_COMPLETED);

        //Implement this method
        User user = createUser();

        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);

        //Create another object of the TestRequest method and explicitly assign this object for Consultation using assignForConsultation() method
        // from consultationController class. Pass the request id of testRequest object.

        TestRequest result = new TestRequest();
        Mockito.when(testRequestUpdateService.assignForConsultation(testRequest.getRequestId(), user)).thenReturn(result);

        result = consultationController.assignForConsultation(testRequest.getRequestId());

        //Use assertThat() methods to perform the following two comparisons
        //  1. the request ids of both the objects created should be same
        //  2. the status of the second object should be equal to 'DIAGNOSIS_IN_PROCESS'
        // make use of assertNotNull() method to make sure that the consultation value of second object is not null
        // use getConsultation() method to get the lab result

        assertNotNull(result);
        assertEquals(result.getRequestId(), testRequest.getRequestId());
        assertEquals(RequestStatus.DIAGNOSIS_IN_PROCESS, result.getStatus());

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
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_throw_exception(){

        Long InvalidRequestId= -34L;
        User user = createUser();

        //Implement this method
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);
        Mockito.when(testRequestUpdateService.assignForConsultation(InvalidRequestId, user)).thenThrow(new AppException("Invalid ID"));

        // Create an object of ResponseStatusException . Use assertThrows() method and pass assignForConsultation() method
        // of consultationController with InvalidRequestId as Id
        ResponseStatusException result = assertThrows(ResponseStatusException.class,()->{
            consultationController.assignForConsultation(InvalidRequestId);
        });

        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "Invalid ID"
        assertNotNull(result);
        assertEquals("Invalid ID", result.getReason());

    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_valid_test_request_id_should_update_the_request_status_and_update_consultation_details(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        User user = createUser();

        //Implement this method

        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);

        //Create an object of CreateConsultationRequest and call getCreateConsultationRequest() to create the object. Pass the above created object as the parameter

        CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);

        //Create another object of the TestRequest method and explicitly update the status of this object
        // to be 'COMPLETED'. Make use of updateConsultation() method from labRequestController class (Pass the previously created two objects as parameters)

        TestRequest mockedResponse = new TestRequest();
        mockedResponse.setStatus(RequestStatus.COMPLETED);

        Mockito.when(testRequestUpdateService.updateConsultation(testRequest.getRequestId(), createConsultationRequest, user)).thenReturn(mockedResponse);

        TestRequest result = consultationController.updateConsultation(testRequest.getRequestId(), createConsultationRequest);

        //Use assertThat() methods to perform the following three comparisons
        //  1. the request ids of both the objects created should be same
        //  2. the status of the second object should be equal to 'COMPLETED'

        assertNotNull(result);
        assertEquals(result.getRequestId(), mockedResponse.getRequestId());
        assertEquals(mockedResponse.getStatus(), RequestStatus.COMPLETED);

    }


    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_test_request_id_should_throw_exception(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        User user = createUser();

        Long InvalidRequestId= -34L;

        //Implement this method
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);

        //Create an object of CreateConsultationRequest and call getCreateConsultationRequest() to create the object. Pass the above created object as the parameter
        CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);

        Mockito.when(testRequestUpdateService.updateConsultation(InvalidRequestId, createConsultationRequest, user)).thenThrow(new AppException("Invalid ID"));
        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateConsultation() method
        // of consultationController with a negative long value as Id and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method
        ResponseStatusException result = assertThrows(ResponseStatusException.class,()->{
            consultationController.updateConsultation(InvalidRequestId, createConsultationRequest);
        });

        //Use assertThat() method to perform the following comparison
        //  the exception message should be contain the string "Invalid ID"
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
        assertEquals("Invalid ID", result.getReason());
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_empty_status_should_throw_exception(){

        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        User user = createUser();

        //Implement this method
        Mockito.when(userLoggedInService.getLoggedInUser()).thenReturn(user);

        //Create an object of CreateConsultationRequest and call getCreateConsultationRequest() to create the object. Pass the above created object as the parameter
        // Set the suggestion of the above created object to null.
        CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);
        createConsultationRequest.setSuggestion(null);

        Mockito.when(testRequestUpdateService.updateConsultation(testRequest.getRequestId(), createConsultationRequest, user)).thenThrow(new AppException());
        // Create an object of ResponseStatusException . Use assertThrows() method and pass updateConsultation() method
        // of consultationController with request Id of the testRequest object and the above created object as second parameter
        //Refer to the TestRequestControllerTest to check how to use assertThrows() method
        ResponseStatusException result = assertThrows(ResponseStatusException.class,()->{
            consultationController.updateConsultation(testRequest.getRequestId(), createConsultationRequest);
        });

        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());

    }

    public CreateConsultationRequest getCreateConsultationRequest(TestRequest testRequest) {

        //Create an object of CreateLabResult and set all the values
        // if the lab result test status is Positive, set the doctor suggestion as "HOME_QUARANTINE" and comments accordingly
        // else if the lab result status is Negative, set the doctor suggestion as "NO_ISSUES" and comments as "Ok"
        // Return the object
        CreateConsultationRequest createConsultationRequest = new CreateConsultationRequest();
        try {
            if (testRequest.getLabResult().getResult().equals(TestStatus.POSITIVE)){
                createConsultationRequest.setSuggestion(DoctorSuggestion.HOME_QUARANTINE);
                createConsultationRequest.setComments("Take care!");
            }
            if (testRequest.getLabResult().getResult().equals(TestStatus.NEGATIVE)){
                createConsultationRequest.setSuggestion(DoctorSuggestion.NO_ISSUES);
                createConsultationRequest.setComments("Ok");
            }
        } catch(Exception e){
            createConsultationRequest.setSuggestion(DoctorSuggestion.NO_ISSUES);
            createConsultationRequest.setComments("Ok");
        }
        return createConsultationRequest;

    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUserName("someuser");
        return user;
    }

}