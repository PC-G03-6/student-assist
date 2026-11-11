package ro.studentassist.buildings.routing;

public record RoutePattern(
        Long routeId,
        Integer directionId,
        String shapeId,
        String headsign,
        double[] lats,
        double[] lons,
        double[] cumulativeMeters) {

    public record NearestPoint(int index, double distanceMeters) {
    }

    /** Finds the shape point closest to the given coordinates. */
    public NearestPoint nearest(double lat, double lon) {
        double cosLat = Math.cos(Math.toRadians(lat));
        double bestSquared = Double.MAX_VALUE;
        int bestIndex = 0;
        for (int i = 0; i < lats.length; i++) {
            double dx = (lon - lons[i]) * cosLat * GeoUtils.METERS_PER_DEGREE;
            double dy = (lat - lats[i]) * GeoUtils.METERS_PER_DEGREE;
            double squared = dx * dx + dy * dy;
            if (squared < bestSquared) {
                bestSquared = squared;
                bestIndex = i;
            }
        }
        return new NearestPoint(bestIndex, Math.sqrt(bestSquared));
    }

    /** Returns the distance in meters along the shape between two point indexes. */
    public double lengthBetween(int fromIndex, int toIndex) {
        return cumulativeMeters[toIndex] - cumulativeMeters[fromIndex];
    }
}
