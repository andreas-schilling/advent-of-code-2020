package org.kiirun.adventofcode.day6;

import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.common.primitives.Chars;
import reactor.core.publisher.Flux;
import reactor.math.MathFlux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day6 {


    public static void main(final String[] args) throws IOException {
        final Flux<Group> groups = Flux.fromIterable(Resources.readLines(Resources.getResource("day6/input1.txt"), StandardCharsets.UTF_8))
                                       .bufferUntil(String::isBlank)
                                       .map(Group::new)
                                       .cache();

        MathFlux.sumInt(groups.map(Group::yesAnswers))
                .subscribe(System.out::println);

        MathFlux.sumInt(groups.map(Group::yesAnswersNewCriterion))
                .subscribe(System.out::println);
    }

    private static class Group {
        private final List<Set<Character>> answers;

        public Group(final List<String> rawValues) {
            answers = rawValues.stream()
                               .filter(Predicate.not(String::isBlank))
                               .map(String::toCharArray)
                               .map(Chars::asList)
                               .map(Set::copyOf)
                               .collect(Collectors.toList());
        }

        public int yesAnswers() {
            if (answers.size() == 1) {
                return answers.get(0).size();
            }
            return answers.stream()
                          .skip(1)
                          .reduce(answers.get(0), Sets::union)
                          .size();
        }

        public int yesAnswersNewCriterion() {
            if (answers.size() == 1) {
                return answers.get(0).size();
            }
            return answers.stream()
                          .skip(1)
                          .reduce(answers.get(0), Sets::intersection)
                          .size();
        }
    }
}
