package org.upgrad.upstac.testrequests.lab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.users.User;

import javax.transaction.Transactional;
import java.time.LocalDateTime;

@Service
@Validated
public class LabResultService {


    @Autowired
    private LabResultRepository labResultRepository;


    private static Logger logger = LoggerFactory.getLogger(LabResultService.class);



    private LabResult createLabResult(User tester, TestRequest testRequest) {
        //Implement this method to create the lab result module service
        // create object of LabResult class and use the setter methods to set tester and testRequest details
        // make use of saveLabResult() method to return the LabResult object

        LabResult labResult = new LabResult();
        labResult.setRequest(testRequest);
        labResult.setTester(tester);
        return  saveLabResult(labResult);

    }

    @Transactional
    LabResult saveLabResult(LabResult labResult) {
        return labResultRepository.save(labResult);
    }



    public LabResult assignForLabTest(TestRequest testRequest, User tester) {

        return createLabResult(tester, testRequest);


    }


    public LabResult updateLabTest(TestRequest testRequest, CreateLabResult createLabResult) {

        //Implement this method to update the lab test
        // create an object of LabResult and make use of setters to set Blood Pressure, Comments,
        // HeartBeat, OxygenLevel, Temperature, Result and UpdatedOn values
        // make use of the saveLabResult() method to return the object of LabResult

        LabResult labResult = labResultRepository.findByRequest(testRequest).orElseThrow(()-> new AppException("Invalid ID"));
        labResult.setBloodPressure(createLabResult.getBloodPressure());
        labResult.setComments(createLabResult.getComments());
        labResult.setHeartBeat(createLabResult.getHeartBeat());
        labResult.setOxygenLevel(createLabResult.getOxygenLevel());
        labResult.setTemperature(createLabResult.getTemperature());
        labResult.setResult(createLabResult.getResult());
        labResult.setUpdatedOn(LocalDateTime.now().toLocalDate());
        return  saveLabResult(labResult);

    }


}
