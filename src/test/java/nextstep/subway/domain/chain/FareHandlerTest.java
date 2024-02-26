package nextstep.subway.domain.chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
class FareHandlerTest {

    @Autowired
    private BasicFareHandler basicFareHandler;

    @Autowired
    private Over10kmFareHandler over10kmFareHandler;

    @Autowired
    private Over50kmFareHandler over50kmFareHandler;

    private FareHandlerFactory fareHandlerFactory;

    @BeforeEach
    void setUp() {
        basicFareHandler.setNextHandler(over10kmFareHandler);
        over10kmFareHandler.setNextHandler(over50kmFareHandler);
        over50kmFareHandler.setNextHandler(null);

        fareHandlerFactory = new FareHandlerFactory(List.of(basicFareHandler, over10kmFareHandler, over50kmFareHandler));
    }

    @ParameterizedTest
    @CsvSource({"9, 1250", "10, 1250"})
    void 이용거리가_10km_이하의_요금을_계산한다(int distance, int expected) {
        long fare = fareHandlerFactory.calculateFare(distance);
        assertThat(fare).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"11, 1350", "16, 1450", "21, 1550", "26, 1650", "31, 1750", "36, 1850", "41, 1950", "46, 2050", "50, 2050"})
    void 이용거리가_10km_초과_50km_이하의_요금을_계산한다(int distance, int expected) {
        long fare = fareHandlerFactory.calculateFare(distance);
        assertThat(fare).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"51, 2150", "58, 2150", "59, 2250"})
    void 이용거리가_50km_초과의_요금을_계산한다(int distance, int expected) {
        long fare = fareHandlerFactory.calculateFare(distance);
        assertThat(fare).isEqualTo(expected);
    }

}
