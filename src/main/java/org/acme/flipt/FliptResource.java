package org.acme.flipt;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/flipt")
public class FliptResource {

    @Inject
    FliptService fliptService;

    @GET
    @Path("/{id}/evaluate/{flagKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response evaluateFlag(@PathParam("id") String entityId, @PathParam("flagKey") String flagKey) {
        try {
            boolean isEnabled = fliptService.evaluateBooleanFlag(flagKey, entityId);
            return Response.ok("{\"flagKey\":\"" + flagKey + "\",\"enabled\":" + isEnabled + "}").build();
        } catch (Exception e) {
            return Response.serverError().entity("{\"error\":\"" + e.getMessage() + "\"}").build();
        }
    }
}
