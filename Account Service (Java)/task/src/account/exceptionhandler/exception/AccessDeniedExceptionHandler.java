package account.exceptionhandler.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


public class AccessDeniedExceptionHandler implements AccessDeniedHandler {

    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("timestamp", String.valueOf(LocalDate.now()));
        responseBody.put("status", HttpServletResponse.SC_FORBIDDEN);
        responseBody.put("error", "Forbidden");
        responseBody.put("message", "Access Denied!");
        responseBody.put("path", request.getRequestURI());
        objectMapper.writeValue(response.getOutputStream(), responseBody);
    }

}
