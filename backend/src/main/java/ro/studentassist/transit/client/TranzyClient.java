package ro.studentassist.transit.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import lombok.RequiredArgsConstructor;
import ro.studentassist.transit.exception.UpstreamException;
import ro.studentassist.transit.dto.*;

@Component
@RequiredArgsConstructor
public class TranzyClient {

    private final RestClient tranzyRestClient;

    public List<Stop> fetchStops() {
        return fetchList("/stops", new ParameterizedTypeReference<List<Stop>>() {
        });
    }

    public List<TransitRoute> fetchRoutes() {
        return fetchList("/routes", new ParameterizedTypeReference<List<TransitRoute>>() {
        });
    }

    public List<Vehicle> fetchVehicles() {
        return fetchList("/vehicles", new ParameterizedTypeReference<List<Vehicle>>() {
        });
    }

    public List<Trip> fetchTrips() {
        return fetchList("/trips", new ParameterizedTypeReference<List<Trip>>() {
        });
    }

    public List<ShapePoint> fetchShapes() {
        return fetchList("/shapes", new ParameterizedTypeReference<List<ShapePoint>>() {
        });
    }

    /** Calls a Tranzy endpoint and returns its JSON array, translating HTTP failures into UpstreamException. */
    private <T> List<T> fetchList(String path, ParameterizedTypeReference<List<T>> type) {
        try {
            List<T> body = tranzyRestClient.get().uri(path).retrieve().body(type);
            return body == null ? List.of() : body;
        } catch (RestClientResponseException ex) {
            int status = ex.getStatusCode().value();
            throw new UpstreamException(status, "Tranzy " + path + " returned status " + status, ex);
        } catch (RestClientException ex) {
            throw new UpstreamException(0, "Tranzy " + path + " is unreachable", ex);
        }
    }
}
