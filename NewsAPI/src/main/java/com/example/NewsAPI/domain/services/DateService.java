package com.example.NewsAPI.domain.services;

import com.example.NewsAPI.exception.DateConvertException;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service
public class DateService {

    public Date definesStartDate(String publicationDate){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date startDate;
            if (publicationDate ==null) {
                startDate = simpleDateFormat.parse("01/01/0001");
            }else {
                startDate = simpleDateFormat.parse(publicationDate);
            }
            return startDate;
        }catch (Exception e){
            throw new DateConvertException(e.getMessage());
        }
    }

    public Date definesEndDate(String publicationDate, Date startDate) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date endDate;
            if (publicationDate == null) {
                endDate = simpleDateFormat.parse("31/12/9999");
            } else {
                endDate = addOneDayToDate(startDate);
            }
            return endDate;
        } catch (Exception e) {
            throw new DateConvertException(e.getMessage());
        }
    }

    public Date addOneDayToDate(Date date){

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE,1);

        return calendar.getTime();
    }
}
