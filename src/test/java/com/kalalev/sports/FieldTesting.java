package com.kalalev.sports;

import com.kalalev.sports.model.Field;
import com.kalalev.sports.model.Order;
import com.kalalev.sports.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.kalalev.sports.repository.FieldRepository;
import com.kalalev.sports.repository.UserRepository;


import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes= SportsApplication.class)
public class FieldTesting {
    @Autowired
    private FieldRepository fieldRepository;
    @Test
    public void testField(){
        Field field = new Field();
        field.getFieldLocation("Pomorie");
        field.getStartedWorkingTime("10:00");
        field.getFinishedWorkingTime("18:00");
        field.getPriceByHour(25);
        fieldRepository.save(field);
    }

    @Test
    public void saveFieldWithTesting()
    {
        Field field = new Field();
        field.setFieldLocation(Integer.parseInt("Burgas"));
        DataIntegrityViolationException dataIntegrityViolationExceptionxception = assertThrows(DataIntegrityViolationException.class,()->{
            fieldRepository.save(field);
        },"Thrown Exception save() saving the user");
        System.out.println(dataIntegrityViolationExceptionxception.getMessage());
    }
}
