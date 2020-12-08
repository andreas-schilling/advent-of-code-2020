package org.kiirun.adventofcode.day4;

import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Range;
import com.google.common.io.Resources;
import com.google.common.primitives.Ints;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class Day4 {
    public static void main(final String[] args) throws IOException {
        final List<Field> fields = List.of(
                new Field("byr", true, new NumberWithRange(1920, 2002)),
                new Field("iyr", true, new NumberWithRange(2010, 2020)),
                new Field("eyr", true, new NumberWithRange(2020, 2030)),
                new Field("hgt", true, new HeightValidator()),
                new Field("hcl", true, Pattern.compile("^#[0-9a-f]{6}").asPredicate()),
                new Field("ecl", true, Predicates.in(List.of("amb", "blu", "brn", "gry", "grn", "hzl", "oth"))),
                new Field("pid", true, Pattern.compile("^[0-9]{8}[0-9]$").asPredicate()),
                new Field("cid", false, anything -> true)
        );
        final Flux<Passport> passports = Flux.fromIterable(Resources.readLines(Resources.getResource("day4/input1.txt"), StandardCharsets.UTF_8))
                                             .bufferUntil(String::isBlank)
                                             .map(Passport::new)
                                             .cache();
        passports.filter(passport -> passport.isValid(fields))
                 .count()
                 .subscribe(System.out::println);

        passports.filter(passport -> passport.isValidNewPolicy(fields))
                 .count()
                 .subscribe(System.out::println);
    }


    private static class Field {
        private final String name;

        private final boolean mandatory;

        private final Predicate<String> validator;

        public Field(final String name, final boolean mandatory, final Predicate<String> validator) {
            this.name = name;
            this.mandatory = mandatory;
            this.validator = validator;
        }

        public String getName() {
            return name;
        }

        public boolean isMandatory() {
            return mandatory;
        }

        public boolean isPresentAndValid(final Map<String, String> values) {
            return (!mandatory || values.containsKey(name)) && validator.test(values.get(name));
        }
    }

    private static class Passport {
        private static final Splitter.MapSplitter TO_KEY_VALUE = Splitter.on(' ').withKeyValueSeparator(':');

        private final Map<String, String> values = new HashMap<>();

        public Passport(final List<String> rawValues) {
            rawValues.stream()
                     .filter(Predicate.not(String::isBlank))
                     .map(TO_KEY_VALUE::split)
                     .forEach(values::putAll);
        }

        public boolean isValid(final List<Field> expectedFields) {
            return expectedFields.stream()
                                 .filter(Field::isMandatory)
                                 .map(Field::getName)
                                 .map(values::containsKey)
                                 .noneMatch(Boolean.FALSE::equals);
        }

        public boolean isValidNewPolicy(final List<Field> expectedFields) {
            return expectedFields.stream()
                                 .filter(Field::isMandatory)
                                 .map(field -> field.isPresentAndValid(values))
                                 .noneMatch(Boolean.FALSE::equals);
        }
    }

    private static class NumberWithRange implements Predicate<String> {
        private final Range<Integer> range;

        public NumberWithRange(final Integer atLeast, final Integer atMost) {
            range = Range.closed(atLeast, atMost);
        }

        @Override
        public boolean test(final String input) {
            return Optional.ofNullable(Ints.tryParse(input))
                           .map(range::contains)
                           .orElse(false);
        }
    }

    private static class HeightValidator implements Predicate<String> {
        @Override
        public boolean test(final String input) {
            if (input.endsWith("cm")) {
                final Integer height = Ints.tryParse(input.replace("cm", ""));
                return height >= 150 && height <= 193;
            }
            if (input.endsWith("in")) {
                final Integer height = Ints.tryParse(input.replace("in", ""));
                return height >= 59 && height <= 76;
            }
            return false;
        }
    }
}
