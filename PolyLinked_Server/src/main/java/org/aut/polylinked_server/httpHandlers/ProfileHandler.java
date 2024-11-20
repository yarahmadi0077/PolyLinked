package org.aut.polylinked_server.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.aut.polylinked_server.dataAccessors.MediaAccessor;
import org.aut.polylinked_server.dataAccessors.ProfileAccessor;
import org.aut.polylinked_server.models.Profile;
import org.aut.polylinked_server.models.Skill;
import org.aut.polylinked_server.models.User;
import org.aut.polylinked_server.utils.JsonHandler;
import org.aut.polylinked_server.utils.MultipartHandler;
import org.aut.polylinked_server.utils.exceptions.NotAcceptableException;
import org.aut.polylinked_server.utils.exceptions.NotFoundException;
import org.aut.polylinked_server.utils.exceptions.UnauthorizedException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

public class ProfileHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String jwt = exchange.getRequestHeaders().getFirst("Authorization");

        try {
            User user = LoginHandler.getUserByToken(jwt);

            switch (method) {
                case "POST":
                case "PUT": {
                    InputStream inputStream = exchange.getRequestBody();
                    Profile profile = new Profile(JsonHandler.getObject(inputStream));

                    try {
                        ProfileAccessor.getProfile(profile.getUserId());
                        ProfileAccessor.updateProfile(profile);
                    } catch (Exception e) {
                        ProfileAccessor.addProfile(profile);
                    }

                    inputStream.close();
                    exchange.sendResponseHeaders(200, 0);
                }
                break;
                case "GET": {
                    String[] path = exchange.getRequestURI().getPath().split("/");
                    if (path.length != 4) throw new NotAcceptableException("Not acceptable");

                    Profile profile = ProfileAccessor.getProfile(path[3]);

                    exchange.sendResponseHeaders(200, 0);
                    OutputStream outputStream = exchange.getResponseBody();
                    JsonHandler.sendObject(outputStream, profile.toJson());

                    outputStream.close();
                }
                break;
                default:
                    exchange.sendResponseHeaders(405, 0);
                    break;
            }
        } catch (UnauthorizedException e) {
            exchange.sendResponseHeaders(401, 0);
        } catch (SQLException e) {
            exchange.sendResponseHeaders(500, 0);
        } catch (NotAcceptableException e) {
            exchange.sendResponseHeaders(406, 0);
        } catch (NotFoundException e) {
            exchange.sendResponseHeaders(404, 0);
        }
        exchange.close();
    }
}