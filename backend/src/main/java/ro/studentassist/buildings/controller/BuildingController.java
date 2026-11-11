package ro.studentassist.buildings;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;
    private final BuildingRouteService buildingRouteService;

    /** Returns all known UBB buildings with their coordinates. */
    @GetMapping
    public List<BuildingResponse> getBuildings() {
        return buildingService.findAll().stream().map(BuildingResponse::from).toList();
    }

    /** Returns transit options and estimated times from the given coordinates to the building. */
    @GetMapping("/{id}/route")
    public BuildingRouteResponse getRoute(
            @PathVariable Long id,
            @RequestParam @DecimalMin("-90.0") @DecimalMax("90.0") double fromLat,
            @RequestParam @DecimalMin("-180.0") @DecimalMax("180.0") double fromLon) {
        return buildingRouteService.findRoute(id, fromLat, fromLon);
    }
}
