package ro.studentassist.transit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TransitExceptionHandler {

    /** Converts a failed Tranzy call into a 502 response, or 503 when Tranzy rate limited us. */
    @ExceptionHandler(UpstreamException.class)
    ProblemDetail handleUpstream(UpstreamException ex) {
        HttpStatus status = ex.getUpstreamStatus() == 429
                ? HttpStatus.SERVICE_UNAVAILABLE
                : HttpStatus.BAD_GATEWAY;
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problem.setTitle("Transit provider error");
        return problem;
    }
}
