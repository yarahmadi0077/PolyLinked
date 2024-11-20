package org.aut.polylinked_server.controllers;

import org.aut.polylinked_server.dataAccessors.FollowAccessor;
import org.aut.polylinked_server.models.Follow;
import org.aut.polylinked_server.utils.exceptions.NotFoundException;
import org.aut.polylinked_server.utils.exceptions.NotAcceptableException;
import java.sql.SQLException;


public class FollowController {
    public static void addFollow(Follow follow) throws SQLException, NotFoundException, NotAcceptableException {
        if (follow == null) {
            throw new NotAcceptableException("Null follow");
        } else if (!UserController.userExistsById(follow.getFollower_id()) || !UserController.userExistsById(follow.getFollowed_id())) {
            throw new NotFoundException("User not found");
        } else if (FollowAccessor.followExists(follow)) {
            throw new NotAcceptableException("Follow already exists");
        } else if (follow.getFollowed_id().equals(follow.getFollower_id())) {
            throw new NotAcceptableException("User Can't follow himself");
        }
        FollowAccessor.addFollow(follow);
    }

    public static void deleteFollow(Follow follow) throws SQLException, NotFoundException, NotAcceptableException {
        if (follow == null) {
            throw new NotAcceptableException("Null follow");
        } else if (!UserController.userExistsById(follow.getFollowed_id()) || !UserController.userExistsById(follow.getFollower_id())) {
            throw new NotFoundException("User not found");
        } else if (!FollowAccessor.followExists(follow)) {
            throw new NotFoundException("Follow not found");
        }
        FollowAccessor.deleteFollow(follow);
    }
}
