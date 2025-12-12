package com.softserve.itacademy.config.security;

import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authz")
@RequiredArgsConstructor
public class AuthorizationService {

    private final ToDoService todoService;
    private final UserService userService;

    public boolean isCurrentUser(long userId) {
        return getCurrentUserId() == userId;
    }

    public boolean canReadToDo(long todoId) {
        User user = userService.readById(getCurrentUserId());
        ToDo todo = todoService.readById(todoId);
        boolean isCollaborator = todo.getCollaborators().stream().anyMatch(c -> c.getId() == user.getId());
        return todo.getOwner().getId() == user.getId() || isCollaborator;
    }

    public boolean isOwnerOfToDo(long todoId) {
        User user = userService.readById(getCurrentUserId());
        ToDo todo = todoService.readById(todoId);
        return todo.getOwner().getId() == user.getId();
    }

    private long getCurrentUserId() {
        WebAuthenticationToken authentication = (WebAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return authentication.getUser().getId();
    }
}
