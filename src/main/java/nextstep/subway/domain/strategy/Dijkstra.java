package nextstep.subway.domain.strategy;

import nextstep.exception.ApplicationException;
import nextstep.subway.domain.PathType;
import nextstep.subway.domain.Section;
import nextstep.subway.domain.Station;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.WeightedMultigraph;

import java.util.List;
import java.util.Objects;

public class Dijkstra implements ShortestPathStrategy {

    private final DijkstraShortestPath dijkstraShortestPath;

    public Dijkstra(List<Section> sections, PathType pathType) {
        WeightedMultigraph<Station, DefaultWeightedEdge> graph = new WeightedMultigraph<>(DefaultWeightedEdge.class);
        addVertexes(sections, graph);
        addEdges(sections, graph, pathType);
        dijkstraShortestPath = new DijkstraShortestPath<>(graph);
    }

    private void addVertexes(List<Section> sections, WeightedMultigraph<Station, DefaultWeightedEdge> graph) {
        for (Section section : sections) {
            graph.addVertex(section.upStation());
            graph.addVertex(section.downStation());
        }
    }

    private void addEdges(List<Section> sections, WeightedMultigraph<Station, DefaultWeightedEdge> graph, PathType pathType) {
        for (Section section : sections) {
            graph.setEdgeWeight(graph.addEdge(section.upStation(), section.downStation()), pathType.getType().apply(section));
        }
    }

    @Override
    public List<Station> findShortestPath(Station source, Station target) {
        GraphPath shortestPath = getPath(source, target);
        validateExistPath(shortestPath);
        return shortestPath.getVertexList();
    }

    @Override
    public long findShortestValue(Station source, Station target) {
        GraphPath shortestPath = getPath(source, target);
        validateExistPath(shortestPath);
        return (long) shortestPath.getWeight();
    }

    private GraphPath getPath(Station source, Station target) {
        try {
            return dijkstraShortestPath.getPath(source, target);
        } catch (IllegalArgumentException e) {
            throw new ApplicationException("노선에 존재하지 않는 지하철역입니다.");
        }
    }

    private void validateExistPath(GraphPath<Station, DefaultWeightedEdge> shortestPath) {
        if (Objects.isNull(shortestPath)) {
            throw new ApplicationException("출발역과 도착역이 연결되어 있지 않습니다.");
        }
    }
}
