package store.app;

import java.util.Scanner;
import java.util.function.Function;

import store.exception.ValidationException;
import store.model.CustomerType;
import store.model.DeliveryZone;

/** Shared console parsing and re-prompt behavior for the two front ends. */
public final class ConsoleInput {
    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        if (scanner == null) {
            throw new ValidationException("A scanner is required.");
        }
        this.scanner = scanner;
    }

    public String line(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public <T> T readParsed(String prompt, Function<String, T> parser) {
        while (true) {
            String raw = line(prompt);
            try {
                return parser.apply(raw);
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public int readInt(String prompt, int minimum, int maximum, String what) {
        return readParsed(prompt, raw -> {
            String text = raw.trim();
            int value = parseInteger(text, capital(what));
            if (value < minimum || value > maximum) {
                throw new ValidationException(capital(what) + " must be between "
                        + minimum + " and " + maximum + ", got " + value + ".");
            }
            return value;
        });
    }

    public double readPositiveAmount(String prompt) {
        return readParsed(prompt, raw -> {
            String text = raw.trim();
            double value;
            try {
                value = Double.parseDouble(text);
            } catch (NumberFormatException e) {
                throw new ValidationException("Price must be a number, got '" + text + "'.");
            }
            if (Double.isNaN(value) || value <= 0) {
                throw new ValidationException("Price must be a positive amount, got " + text + ".");
            }
            return value;
        });
    }

    public CustomerType readCustomerType(String prompt) {
        return readParsed(prompt, raw -> raw.trim().isEmpty()
                ? CustomerType.STANDARD : CustomerType.fromInput(raw));
    }

    public DeliveryZone readDeliveryZone(String prompt) {
        return readParsed(prompt, DeliveryZone::fromInput);
    }

    public static int parseInteger(String raw, String what) {
        String text = raw == null ? "" : raw.trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException(what
                    + " must be a whole number, got '" + text + "'.");
        }
    }

    private static String capital(String value) {
        return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
