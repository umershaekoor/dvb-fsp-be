package org.dvbern.playground.fingersignaturepoc;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MainResource {

    private static final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();

    @POST
    @Path("/generate")
    public Response generateToken() {
        String token = UUID.randomUUID().toString();

        TokenInfo info = new TokenInfo();
        info.token = token;
        info.orgId = "org-123";   // hardcoded for POC
        info.userId = "user-456"; // hardcoded for POC
        info.expiry = Instant.now().plusSeconds(300); // 5 minutes

        tokenStore.put(token, info);

        String url = "http://localhost:4200/sign?token=" + token;
        return Response.ok(Map.of("url", url)).build();
    }

    @POST
    @Path("/validate")
    public Response validateToken(Map<String, String> body) {
        String token = body.get("token");
        TokenInfo info = tokenStore.get(token);

        if (info == null || Instant.now().isAfter(info.expiry)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "Invalid or expired token"))
                .build();
        }

        // Mark token as validated
        info.validated = true;

        return Response.ok(info).build();
    }

    @POST
    @Path("/signature")
    public Response saveSignature(Map<String, String> body) {
        String token = body.get("token");
        String signatureImage = body.get("signatureImage");

        TokenInfo info = tokenStore.get(token);

        if (info == null || !info.validated) {
            return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "Token not validated"))
                .build();
        }

        // Store signature
        info.signatureImage = signatureImage;

        return Response.ok(Map.of("status", "Signature saved")).build();
    }
}
