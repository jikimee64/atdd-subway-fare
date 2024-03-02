package nextstep.subway.domain;

import nextstep.exception.ApplicationException;
import nextstep.subway.domain.strategy.Dijkstra;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static nextstep.subway.fixture.LineFixture.*;
import static nextstep.subway.fixture.StationFixture.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class PathTest {

    private static Station 강남역;
    private static Station 선릉역;
    private static Station 양재역;
    private static Station 역삼역;
    private static Station 신대방역;
    private static Station 신림역;
    private static Station 봉천역;

    private static Line 신분당선;
    private static Line 분당선;
    private static Line 일호선;
    private static Line 이호선;
    private static Line 삼호선;

    private static Section 강남역_선릉역_구간;
    private static Section 선릉역_양재역_구간;
    private static Section 양재역_역삼역_구간;
    private static Section 역삼역_강남역_구간;
    private static Section 신대방역_신림역_구간;

    /**
     * 역삼역    --- *1호선*(30D, 30T) ---        양재역
     * |                                             |
     * *2호선*(40D, 40T)                        *분당선*(20D, 20T)
     * |                                             |
     * 강남역    --- *신분당호선*(10D, 10T) ---    선릉역
     * <p>
     * 강남역    --- *3호선*(5D, 50T) ---    선릉역
     */
    @BeforeAll
    static void setUp() {
        강남역 = GANGNAM_STATION.toStation(1L);
        선릉역 = SEOLLEUNG_STATION.toStation(2L);
        양재역 = YANGJAE_STATION.toStation(3L);
        역삼역 = YEOKSAM_STATION.toStation(4L);
        신대방역 = SINDAEBANG_STATION.toStation(5L);
        신림역 = SILLIM_STATION.toStation(6L);
        봉천역 = SILLIM_STATION.toStation(7L);

        신분당선 = SHINBUNDANG_LINE.toLine(1L);
        분당선 = BUNDANG_LINE.toLine(2L);
        일호선 = ONE_LINE.toLine(3L);
        이호선 = TWO_LINE.toLine(4L);
        삼호선 = TWO_LINE.toLine(5L);

        강남역_선릉역_구간 = new Section(신분당선, 강남역, 선릉역, 10L, 10L);
        선릉역_양재역_구간 = new Section(분당선, 선릉역, 양재역, 20L, 20L);
        양재역_역삼역_구간 = new Section(일호선, 역삼역, 양재역, 30L, 30L);
        역삼역_강남역_구간 = new Section(일호선, 강남역, 역삼역, 40L, 40L);
        신대방역_신림역_구간 = new Section(삼호선, 신대방역, 신림역, 50L, 50L);
    }

    @Test
    void 실패_출발역과_도착역이_같은_경우_경로를_조회할_수_없다() {
        Path path = createPathBy(PathType.DISTANCE);
        assertThatThrownBy(() -> path.findShortestPath(강남역, 강남역))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("출발역과 도착역이 같은 경우 경로를 조회할 수 없습니다.");
    }

    @Test
    void 실패_출발역과_도착역이_연결되어_있지_않은_경우_경로를_조회할_수_없다() {
        Path path = createPathBy(PathType.DISTANCE);
        assertThatThrownBy(() -> path.findShortestPath(강남역, 신대방역))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("출발역과 도착역이 연결되어 있지 않습니다.");
    }

    @ParameterizedTest
    @MethodSource("provideNonExistStation")
    void 실패_출발역이나_도착역이_노선에_존재하지_않을_경우_경로를_조회할_수_없다(Station source, Station target) {
        Path path = createPathBy(PathType.DISTANCE);
        assertThatThrownBy(() -> path.findShortestPath(source, target))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("노선에 존재하지 않는 지하철역입니다.");
    }

    private static Stream<Arguments> provideNonExistStation() {
        return Stream.of(
                Arguments.of(강남역, 봉천역),
                Arguments.of(봉천역, 강남역)
        );
    }

    @Test
    void 성공_출발역과_도착역이_연결되어_있을_경우_최단_경로에_속하는_지하철역을_조회할_수_있다() {
        Path path = createPathBy(PathType.DISTANCE);
        List<Station> stations = path.findShortestPath(강남역, 역삼역);
        assertThat(stations).hasSize(2)
                .extracting("id", "name")
                .containsExactly(
                        tuple(1L, "강남역"),
                        tuple(4L, "역삼역")
                );
    }

    @Test
    void 성공_출발역과_도착역이_연결되어_있을_경우_최단_경로에_속하는_구간을_조회할_수_있다() {
        Path path = createPathBy(PathType.DISTANCE);
        List<Section> sections = path.findShortestEdges(강남역, 양재역);
        assertThat(sections).hasSize(2)
                .extracting("line", "upStation", "downStation")
                .containsExactly(
                        tuple(신분당선, 강남역, 선릉역),
                        tuple(분당선, 선릉역, 양재역)
                );
    }

    @Test
    void 성공_출발역과_도착역이_연결되어_있을_경우_최단_경로의_거리를_조회할_수_있다() {
        Path path = createPathBy(PathType.DISTANCE);
        assertThat(path.findShortestValue(강남역, 역삼역)).isEqualTo(40);
    }

    @Test
    void 성공_출발역과_도착역이_연결되어_있을_경우_최단_경로의_소요시간을_조회할_수_있다() {
        Path path = createPathBy(PathType.DURATION);
        path.findShortestEdges(강남역, 역삼역);
        assertThat(path.findShortestValue(강남역, 역삼역)).isEqualTo(40);
    }

    private Path createPathBy(PathType pathType) {
        return new Path(
                new Dijkstra(List.of(
                        강남역_선릉역_구간,
                        선릉역_양재역_구간,
                        양재역_역삼역_구간,
                        역삼역_강남역_구간,
                        신대방역_신림역_구간
                ), pathType), pathType);
    }

}
