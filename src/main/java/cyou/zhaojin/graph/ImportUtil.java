package cyou.zhaojin.graph;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cyou.zhaojin.obj.Edge;
import cyou.zhaojin.obj.Vertex;

import java.io.IOException;
import java.util.List;

/**
 * @author Zhao JIN
 */
public class ImportUtil {
    private final static ObjectMapper om = new ObjectMapper();

    public static List<Vertex> getVertex(String vertex) throws IOException {
        return om.readValue(ImportUtil.class.getClassLoader().getResourceAsStream(vertex), new TypeReference<List<Vertex>>(){});
    }

    public static List<Edge> getEdge(String edge) throws IOException {
        return om.readValue(ImportUtil.class.getClassLoader().getResourceAsStream(edge), new TypeReference<List<Edge>>() {});
    }
}
