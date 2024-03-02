package nextstep.subway.domain.chain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Over50kmFareHandlerTest {

    private Over50kmFareHandler over50kmFareHandler;

    @BeforeEach
    void setUp(){
        over50kmFareHandler = new Over50kmFareHandler();
    }

    @ParameterizedTest
    @CsvSource({"51, 100", "58, 100", "59, 200"})
    void 이용거리가_50km_초과시_8km마다_요금이_100원씩_증가한다(long distance, long price){
        // when
        long fare = over50kmFareHandler.calculate(distance);

        // then
        assertThat(fare).isEqualTo(price);
    }

}
