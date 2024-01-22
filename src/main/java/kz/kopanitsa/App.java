package kz.kopanitsa;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class App {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the path to the JSON file, origin_name, destination_name: ");
        String filePath = scanner.next();
        String originName = scanner.next();
        String destinationName = scanner.next();

        try (FileReader fileReader = new FileReader(filePath)) {
            JsonArray tickets = JsonParser.parseReader(fileReader).getAsJsonObject().getAsJsonArray("tickets");
            calculateMinFlightTime(tickets, originName, destinationName);
            calculatePriceStats(tickets, originName, destinationName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void calculateMinFlightTime(JsonArray tickets, String origin, String destination) {
        Map<String, Integer> minFlightTimeMap = new HashMap<>();
        for (JsonElement element : tickets) {
            JsonObject ticket = element.getAsJsonObject();
            String originCode = ticket.get("origin_name").getAsString();
            String destinationCode = ticket.get("destination_name").getAsString();
            String carrier = ticket.get("carrier").getAsString();
            String departureDate = ticket.get("departure_date").getAsString();
            String departureTime = ticket.get("departure_time").getAsString();
            String arrivalDate = ticket.get("arrival_date").getAsString();
            String arrivalTime = ticket.get("arrival_time").getAsString();
            LocalDateTime departureDateTime = parseDateTime(departureDate, departureTime);
            LocalDateTime arrivalDateTime = parseDateTime(arrivalDate, arrivalTime);
            if (origin.equals(originCode) && destination.equals(destinationCode)) {
                int stopsTime = ticket.get("stops").getAsInt();
                int flightTime = calculateFlightDuration(departureDateTime, arrivalDateTime);
                int time = stopsTime + flightTime;
                minFlightTimeMap.compute(carrier, (key, value) -> (value == null || time < value) ? time : value);
            }
        }
        System.out.println("Min Flight Time:");
        minFlightTimeMap.forEach((currentCarrier, minFlightTime) -> {
            System.out.println("Carrier: " + currentCarrier + ", Min Flight Time: " + minFlightTime + " minutes");
        });
    }


    private static LocalDateTime parseDateTime(String date, String time) {
        String dateTimeString = date + " " + time;
        int indexOfDot = time.indexOf(':');
        String result = indexOfDot != -1 ? time.substring(0, indexOfDot) : time;
         if(result.length() == 1) {
             DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");
             return LocalDateTime.parse(dateTimeString, formatter);
         }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
        return LocalDateTime.parse(dateTimeString, formatter);
    }

    private static int calculateFlightDuration(LocalDateTime departure, LocalDateTime arrival) {
        Duration duration = Duration.between(departure, arrival);
        return (int) duration.toMinutes();
    }

    private static void calculatePriceStats(JsonArray tickets, String origin, String destination) {
        List<Integer> prices = new ArrayList<>();
        for (JsonElement element : tickets) {
            JsonObject ticket = element.getAsJsonObject();
            String originCode = ticket.get("origin_name").getAsString();
            String destinationCode = ticket.get("destination_name").getAsString();
            if (originCode.equals(origin) && destinationCode.equals(destination)) {
                int price = ticket.get("price").getAsInt();
                prices.add(price);
            }
        }
        if (!prices.isEmpty()) {
            double averagePrice = prices.stream().mapToInt(Integer::intValue).average().orElse(0);
            System.out.println("\nAverage Price: " + averagePrice);
            Collections.sort(prices);
            int median;
            if (prices.size() % 2 == 0) {
                median = (prices.get(prices.size() / 2 - 1) + prices.get(prices.size() / 2)) / 2;
            } else {
                median = prices.get(prices.size() / 2);
            }
            System.out.println("Median Price: " + median);
            System.out.println("The difference between the average price and the median is: " + (averagePrice-median));
        }
    }
}
