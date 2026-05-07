package com.services.user;

import org.json.JSONObject;

import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @EJB
    private UserService userService;


    @POST
    @Path("/register")
    public Response register(String body) {
        JSONObject json = new JSONObject(body);
        String result = userService.register(
                json.getString("username"),
                json.getString("password"),
                json.getString("role"),
                json.optString("profession", null),
                json.optDouble("initialBalance", 0.0));
        return Response.ok(result).build();
    }


    @POST
    @Path("/login")
    public Response login(String body) {
        JSONObject json = new JSONObject(body);
        String result = userService.login(
                json.getString("username"),
                json.getString("password"));
        return Response.ok(result).build();
    }


    @POST
    @Path("/{id}/add-funds")
    public Response addFunds(@PathParam("id") int userId, String body) {
        JSONObject json = new JSONObject(body);
        String result = userService.addFunds(userId, json.getDouble("amount"));
        return Response.ok(result).build();
    }

    @GET
    @Path("/{id}/wallet")
    public Response getWallet(@PathParam("id") int userId) {
        return Response.ok(userService.getWallet(userId)).build();
    }

    @POST
    @Path("/{id}/deduct")
    public Response deduct(@PathParam("id") int userId, String body) {
        JSONObject json = new JSONObject(body);
        String result = userService.deductWallet(userId, json.getDouble("amount"));
        return Response.ok(result).build();
    }


    @POST
    @Path("/{id}/refund")
    public Response refund(@PathParam("id") int userId, String body) {
        JSONObject json = new JSONObject(body);
        String result = userService.refundWallet(userId, json.getDouble("amount"));
        return Response.ok(result).build();
    }

    @GET
    @Path("/all")
    public Response getAllUsers() {
        return Response.ok(userService.getAllUsers()).build();
    }
}