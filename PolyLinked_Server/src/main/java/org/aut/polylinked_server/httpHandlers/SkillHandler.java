package org.aut.polylinked_server.httpHandlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.aut.polylinked_server.dataAccessors.EducationAccessor;
import org.aut.polylinked_server.dataAccessors.ProfileAccessor;
import org.aut.polylinked_server.dataAccessors.SkillsAccessor;
import org.aut.polylinked_server.models.Skill;
import org.aut.polylinked_server.models.User;
import org.aut.polylinked_server.utils.JsonHandler;
import org.aut.polylinked_server.utils.MultipartHandler;
import org.aut.polylinked_server.utils.exceptions.NotAcceptableException;
import org.aut.polylinked_server.utils.exceptions.NotFoundException;
import org.aut.polylinked_server.utils.exceptions.UnauthorizedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

public class SkillHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String jwt = exchange.getRequestHeaders().getFirst("Authorization");
        String[] path = exchange.getRequestURI().getPath().split("/");

        try {
            User user = LoginHandler.getUserByToken(jwt);
            switch (method) {
                case "PUT":
                case "POST": {
                    InputStream inputStream = exchange.getRequestBody();

                    Skill skill = new Skill(JsonHandler.getObject(inputStream));
                    if (!skill.getUserId().equals(user.getUserId())) throw new UnauthorizedException("Unauthorized");

                    try {
                        SkillsAccessor.getSkill(skill.getSkillId());
                        SkillsAccessor.updateSkill(skill);
                    } catch (Exception e) {
                        SkillsAccessor.addSkill(skill);
                    }

                    inputStream.close();
                    exchange.sendResponseHeaders(200, 0);
                }
                break;
                case "GET": {
                    if (path.length != 4) throw new NotAcceptableException("Invalid path");

                    ArrayList<Skill> skills = SkillsAccessor.getSkillsOfUser(path[3]);
                    if (!skills.isEmpty()) {
                        exchange.getResponseHeaders().add("X-Total-Count", Integer.toString(skills.size()));
                        exchange.sendResponseHeaders(200, 0);

                        OutputStream outputStream = exchange.getResponseBody();
                        MultipartHandler.writeObjectArray(outputStream, skills);
                        outputStream.close();
                    } else throw new NotFoundException("Not Found");
                }
                break;
                case "DELETE": {
                    if (path.length != 4) throw new NotAcceptableException("Invalid path");
                    Skill skill = SkillsAccessor.getSkill(path[3]);
                    if (!skill.getUserId().equals(user.getUserId()))
                        throw new UnauthorizedException("Unauthorized");

                    SkillsAccessor.deleteSkill(skill.getSkillId());
                    exchange.sendResponseHeaders(200, 0);
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
