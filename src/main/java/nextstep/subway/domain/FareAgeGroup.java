package nextstep.subway.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.function.Function;

@Getter
@AllArgsConstructor
public enum FareAgeGroup {
    CHILD(6, 12,  fare -> fare - 350 - ((fare - 350) * 50 / 100)),
    TEENAGER(13, 18,  fare -> fare - 350 - ((fare - 350) * 20 / 100)),
    ADULT(19, 100, fare -> fare);

    private final int minAge;
    private final int maxAge;
    private final Function<Integer, Integer> fareCalculator;

    public static FareAgeGroup of(int age) {
        return Arrays.stream(FareAgeGroup.values())
                .filter(group -> age >= group.minAge && age <= group.maxAge)
                .findFirst()
                .orElse(ADULT);
    }

    public int calculateFare(int fare) {
        return this.fareCalculator.apply(fare);
    }
}
