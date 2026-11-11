package ro.studentassist.buildings;

final class GeoUtils {

    static final double METERS_PER_DEGREE = 111_320.0;

    private GeoUtils() {
    }

    /** Returns the approximate distance in meters between two coordinates, accurate at city scale. */
    static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double meanLatRadians = Math.toRadians((lat1 + lat2) / 2);
        double dx = (lon2 - lon1) * Math.cos(meanLatRadians) * METERS_PER_DEGREE;
        double dy = (lat2 - lat1) * METERS_PER_DEGREE;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
