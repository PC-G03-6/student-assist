package ro.studentassist.transit;

public class UpstreamException extends RuntimeException {

    private final int upstreamStatus;

    public UpstreamException(int upstreamStatus, String message, Throwable cause) {
        super(message, cause);
        this.upstreamStatus = upstreamStatus;
    }

    public int getUpstreamStatus() {
        return upstreamStatus;
    }
}
