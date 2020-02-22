package com.gmail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is test app.
 */
public class App {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private static final String INPUT_FILE_NAME = "input.txt";
    private static final String OUTPUT_FILE_NAME = "timetable.txt";
    private static final String INPUT_LINE_DELIMITER = " ";
    private static final String OUTPUT_LINE_DELIMITER = " ";
    private static final int TIME_STRING_LENGTH = 12;
    
    private Logger logger = Logger.getGlobal();
    
    public static void main(String[] args) {
        new App().start();
    }
    
    private void start() {
        Set<TimeOffer> timeOffers = parseTimetable();
        Set<TimeOffer> timeTable = filterEfficient(timeOffers);
        writeTimetable(timeTable);
    }
    
    private Set<TimeOffer> filterEfficient(Set<TimeOffer> timeOffers) {
        Set<TimeOffer> efficientTimeOffers = new TreeSet<>();
        
        timeOffers.stream()
            .filter((timeOffer -> ChronoUnit.HOURS.between(timeOffer.getStartTime(), timeOffer.getEndTime()) <= 1))
            .forEach(candidateTimeOffer -> {
                EfficientSelectMode mode = EfficientSelectMode.ADD;
                
                for (TimeOffer efficientTimeOffer : efficientTimeOffers) {
                    mode = selectEfficientTimeOffer(efficientTimeOffer, candidateTimeOffer);
                    
                    if (EfficientSelectMode.REPLACE.equals(mode)) {
                        efficientTimeOffers.remove(efficientTimeOffer);
                        efficientTimeOffers.add(candidateTimeOffer);
                    }
                    
                    if (EfficientSelectMode.REPLACE.equals(mode) || EfficientSelectMode.SKIP.equals(mode)) {
                        break;
                    }
                }
                
                if (EfficientSelectMode.ADD.equals(mode)) {
                    efficientTimeOffers.add(candidateTimeOffer);
                }
            });
        
        return efficientTimeOffers;
    }
    
    private EfficientSelectMode selectEfficientTimeOffer(TimeOffer efficientTimeOffer, TimeOffer candidateTimeOffer) {
        EfficientSelectMode mode = EfficientSelectMode.ADD;
        
        LocalTime startTimeEfficient = efficientTimeOffer.getStartTime();
        LocalTime endTimeEfficient = efficientTimeOffer.getEndTime();
        
        LocalTime startTimeCandidate = candidateTimeOffer.getStartTime();
        LocalTime endTimeCandidate = candidateTimeOffer.getEndTime();
        Company companyCandidate = candidateTimeOffer.getCompany();
        
        if (startTimeCandidate.equals(startTimeEfficient) || endTimeCandidate.equals(endTimeEfficient)) {
            mode = EfficientSelectMode.SKIP;
        }
        
        if (startTimeCandidate.equals(startTimeEfficient) && endTimeCandidate.isBefore(endTimeEfficient)) {
            mode = EfficientSelectMode.REPLACE;
        }
        
        if (endTimeCandidate.equals(endTimeEfficient) && startTimeCandidate.isAfter(startTimeEfficient)) {
            mode = EfficientSelectMode.REPLACE;
        }
        
        if (Company.POSH.equals(companyCandidate) && startTimeCandidate.equals(startTimeEfficient) && endTimeCandidate.equals(endTimeEfficient)) {
            mode = EfficientSelectMode.REPLACE;
        }
        
        if (startTimeCandidate.isAfter(startTimeEfficient) && endTimeCandidate.isBefore(endTimeEfficient)) {
            mode = EfficientSelectMode.REPLACE;
        }
        
        return mode;
    }
    
    private Set<TimeOffer> parseTimetable() {
        Set<TimeOffer> timeOffers = new HashSet<>();
        
        try (Stream<String> stream = Files.lines(Paths.get(INPUT_FILE_NAME))) {
            timeOffers = stream.map(inputLine -> {
                String[] inputArray = inputLine.split(INPUT_LINE_DELIMITER);
                
                boolean isValid = validateInputData(inputArray);
                
                if (!isValid) {
                    logger.log(Level.WARNING, "Validation exception");
                    return null;
                }
                
                return convertInputData(inputArray);
            }).collect(Collectors.toSet());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Exception when reading input file. Inner exception: " + e.getMessage());
        }
        
        timeOffers.remove(null);
        
        return timeOffers;
    }
    
    private TimeOffer convertInputData(String[] inputArray) {
        try {
            Company company = Company.fromValue(inputArray[0]);
            LocalTime startTime = LocalTime.parse(inputArray[1], dateTimeFormatter);
            LocalTime endTime = LocalTime.parse(inputArray[2], dateTimeFormatter);
            
            return new TimeOffer(company, startTime, endTime);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            logger.log(Level.INFO, "Cannot parse the line. Inner exception: " + e.getMessage());
        }
        
        return null;
    }
    
    private boolean validateInputData(String[] inputArray) {
        if (inputArray.length < 2) {
            return false;
        }
        
        for (String input : inputArray) {
            if (input == null || input.isEmpty()) {
                return false;
            }
        }
        
        return true;
    }
    
    private void writeTimetable(Set<TimeOffer> timeTable) {
        final Company[] previousCompany = {null};
        
        List<StringBuilder> timeTableStrings = timeTable.stream().map(timeOffer -> {
            Company currentCompany = timeOffer.getCompany();
            int capacity = TIME_STRING_LENGTH + currentCompany.toString().length();
            StringBuilder stringBuilder = new StringBuilder(capacity);
            
            if (previousCompany[0] != null && !previousCompany[0].equals(currentCompany)) {
                stringBuilder.append(System.lineSeparator());
            }
            
            stringBuilder.append(currentCompany).append(OUTPUT_LINE_DELIMITER).append(timeOffer.getStartTime()).append(OUTPUT_LINE_DELIMITER).append(timeOffer.getEndTime());
            previousCompany[0] = currentCompany;
            
            return stringBuilder;
        }).collect(Collectors.toList());
        
        try {
            Files.write(Paths.get(OUTPUT_FILE_NAME), timeTableStrings, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Exception when writing to file. Inner exception: " + e.getMessage());
        }
    }
    
    private enum EfficientSelectMode {
        ADD,
        REPLACE,
        SKIP
    }
}
