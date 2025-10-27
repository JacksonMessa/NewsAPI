package com.example.NewsAPI.domain.services;

import com.example.NewsAPI.exception.DateConvertException;
import org.junit.jupiter.api.*;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;



class DateServiceTest {

    DateService dateService;

    @BeforeEach
    public void setup(){
        dateService = new DateService();
    }

    @Nested
    class definesStartDate{

        @DisplayName("Should return the day sent in date format")
        @Test
        void definesStartDateTestSuccess(){
            //Arrange
            String publicationDate = "24/10/2025";

            ZonedDateTime zonedExpectedDate = LocalDate.of(2025, 10, 24).atStartOfDay(ZoneId.systemDefault());
            Date expectedDate = Date.from(zonedExpectedDate.toInstant());

            //Act
            Date returnedDate = dateService.definesStartDate(publicationDate);

            //Assert
            assertEquals(expectedDate,returnedDate);

        }

        @DisplayName("Should return 01/01/0001 in date format when publication date is null")
        @Test
        void definesStartDateTestPublicationDateNullSuccess() throws ParseException {
            //Arrange
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date expectedDate = simpleDateFormat.parse("01/01/0001");


            //Act
            Date returnedDate = dateService.definesStartDate(null);

            //Assert
            assertEquals(expectedDate,returnedDate);

        }

        @DisplayName("Should throw DateConvertException when the date sent is in an unexpected format")
        @Test
        void definesStartDateTestDateConvertExceptionFailure(){
            //Arrange
            String publicationDate = "24-10-2025";

            //Act / Assert
            assertThrows(DateConvertException.class,() -> dateService.definesStartDate(publicationDate));
        }
    }

    @Nested
    class definesEndDate{

        @DisplayName("Should return the sent start day added to a day in date format")
        @Test
        void definesEndDateTestSuccess(){
            //Arrange
            String publicationDate = "24/10/2025";

            ZonedDateTime zonedStartDate = LocalDate.of(2025, 10, 24).atStartOfDay(ZoneId.systemDefault());
            Date startDate = Date.from(zonedStartDate.toInstant());

            ZonedDateTime zonedExpectedDate = LocalDate.of(2025, 10, 25).atStartOfDay(ZoneId.systemDefault());
            Date expectedDate = Date.from(zonedExpectedDate.toInstant());

            //Act
            Date returnedDate = dateService.definesEndDate(publicationDate,startDate);

            //Assert
            assertEquals(expectedDate,returnedDate);

        }

        @DisplayName("Should return 31/12/9999 in date format when publication date is null")
        @Test
        void definesEndDateTestPublicationDateNullSuccess(){
            ZonedDateTime zonedDateTime = LocalDate.of(9999, 12, 31).atStartOfDay(ZoneId.systemDefault());
            Date expectedDate = Date.from(zonedDateTime.toInstant());

            //Act
            Date returnedDate = dateService.definesEndDate(null,null);

            //Assert
            assertEquals(expectedDate,returnedDate);
        }

        @DisplayName("Should throw RuntimeException when the start date sent is null")
        @Test
        void definesStartDateTestDateConvertExceptionFailure(){
            //Arrange
            String publicationDate = "24/10/2025";

            //Act / Assert
            assertThrows(RuntimeException.class,() -> dateService.definesEndDate(publicationDate,null));
        }
    }

    @Nested
    class addOneDayToDate{
        @DisplayName("Should return the day sent plus one day")
        @Test
        void addOneDayToDateSuccess(){
            //Arrange
            ZonedDateTime zonedDate = LocalDate.of(2025, 10, 24).atStartOfDay(ZoneId.systemDefault());
            Date date = Date.from(zonedDate.toInstant());

            ZonedDateTime zonedDateTime = LocalDate.of(2025, 10, 25).atStartOfDay(ZoneId.systemDefault());
            Date expectedDate = Date.from(zonedDateTime.toInstant());

            //Act
            Date returnedDate = dateService.addOneDayToDate(date);

            //Assert
            assertEquals(expectedDate,returnedDate);
        }

        @DisplayName("Should return the first day of the next month when sent the last day of the previous month")
        @Test
        void addOneDayToDateMonthsEndSuccess(){
            //Arrange
            ZonedDateTime zonedDate = LocalDate.of(2025, 9, 30).atStartOfDay(ZoneId.systemDefault());
            Date date = Date.from(zonedDate.toInstant());

            ZonedDateTime zonedDateTime = LocalDate.of(2025, 10, 1).atStartOfDay(ZoneId.systemDefault());
            Date expectedDate = Date.from(zonedDateTime.toInstant());

            //Act
            Date returnedDate = dateService.addOneDayToDate(date);

            //Assert
            assertEquals(expectedDate,returnedDate);
        }

        @DisplayName("Should return the first day of the next year when sent the last day of the previous year")
        @Test
        void addOneDayToDateYearsEndSuccess(){
            //Arrange
            ZonedDateTime zonedDate = LocalDate.of(2025, 12, 31).atStartOfDay(ZoneId.systemDefault());
            Date date = Date.from(zonedDate.toInstant());

            ZonedDateTime zonedDateTime = LocalDate.of(2026, 1, 1).atStartOfDay(ZoneId.systemDefault());
            Date expectedDate = Date.from(zonedDateTime.toInstant());

            //Act
            Date returnedDate = dateService.addOneDayToDate(date);

            //Assert
            assertEquals(expectedDate,returnedDate);
        }

        @DisplayName("Should return February 29th when sending February 28th in a leap year")
        @Test
        void addOneDayToDateLeapYearSuccess(){
            //Arrange
            ZonedDateTime zonedDate = LocalDate.of(2028, 2, 28).atStartOfDay(ZoneId.systemDefault());
            Date date = Date.from(zonedDate.toInstant());

            ZonedDateTime zonedDateTime = LocalDate.of(2028, 2, 29).atStartOfDay(ZoneId.systemDefault());
            Date expectedDate = Date.from(zonedDateTime.toInstant());

            //Act
            Date returnedDate = dateService.addOneDayToDate(date);

            //Assert
            assertEquals(expectedDate,returnedDate);
        }

        @DisplayName("Should throw NullPointException when the date sent is null")
        @Test
        void definesStartDateTestDateConvertExceptionFailure(){
            //Arrange
            Date publicationDate = null;

            //Act / Assert
            assertThrows(NullPointerException.class,() -> dateService.addOneDayToDate(publicationDate));
        }
    }



}