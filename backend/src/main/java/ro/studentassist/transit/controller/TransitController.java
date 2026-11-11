package ro.studentassist.transit;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transit")
@RequiredArgsConstructor
public class TransitController {

    private final TransitService transitService;

    /** Returns all transit stops. */
    @GetMapping("/stops")
    public List<Stop> getStops() {
        return transitService.getStops();
    }

    /** Returns all transit routes. */
    @GetMapping("/routes")
    public List<TransitRoute> getRoutes() {
        return transitService.getRoutes();
    }

    /** Returns live vehicle positions, optionally limited to vehicles with a recent update and a route. */
    @GetMapping("/vehicles")
    public List<Vehicle> getVehicles(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return activeOnly ? transitService.getActiveVehicles() : transitService.getVehicles();
    }
}
