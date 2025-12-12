package com.softserve.itacademy.controller;

import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;



@Controller
@RequestMapping("/todos")
@RequiredArgsConstructor
public class ToDoController {

    private final ToDoService todoService;
    private final TaskService taskService;
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN') or @authz.isCurrentUser(#ownerId)")
    @GetMapping("/create/users/{owner_id}")
    public String createToDoForm(@PathVariable("owner_id") long ownerId, Model model) {
        model.addAttribute("todo", new ToDo());
        model.addAttribute("ownerId", ownerId);
        return "create-todo";
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.isCurrentUser(#ownerId)")
    @PostMapping("/create/users/{owner_id}")
    public String createToDo(@PathVariable("owner_id") long ownerId,
                             @Validated @ModelAttribute("todo") ToDo todo, BindingResult result) {
        if (result.hasErrors()) {
            return "create-todo";
        }
        todo.setCreatedAt(LocalDateTime.now());
        todo.setOwner(userService.readById(ownerId));
        todoService.create(todo);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.canReadToDo(#id)")
    @GetMapping("/{id}/read")
    public String read(@PathVariable long id, Model model) {
        ToDo todo = todoService.readById(id);
        List<Task> tasks = taskService.getByTodoId(id);
        List<User> users = userService.getAll().stream()
                .filter(user -> user.getId() != todo.getOwner().getId())
                .filter(user -> todo.getCollaborators().stream().allMatch((collaborator)
                        -> collaborator.getId() != user.getId()))
                .collect(Collectors.toList());
        model.addAttribute("todo", todo);
        model.addAttribute("tasks", tasks);
        model.addAttribute("users", users);
        return "read-todo";
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.isOwnerOfToDo(#todoId)")
    @GetMapping("/{todo_id}/update/users/{owner_id}")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId, Model model) {
        ToDo todo = todoService.readById(todoId);
        model.addAttribute("todo", todo);
        return "update-todo";
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.isOwnerOfToDo(#todoId)")
    @PostMapping("/{todo_id}/update/users/{owner_id}")
    public String update(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId,
                         @Validated @ModelAttribute("todo") ToDo todo, BindingResult result, Model model) {
        if (result.hasErrors()) {
            todo.setOwner(userService.readById(ownerId));
            return "update-todo";
        }
        ToDo oldTodo = todoService.readById(todoId);
        todo.setOwner(oldTodo.getOwner());
        todo.setCollaborators(oldTodo.getCollaborators());
        todoService.update(todo);
        model.addAttribute(todo);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.isOwnerOfToDo(#todoId)")
    @GetMapping("/{todo_id}/delete/users/{owner_id}")
    public String delete(@PathVariable("todo_id") long todoId, @PathVariable("owner_id") long ownerId) {
        todoService.delete(todoId);
        return "redirect:/todos/all/users/" + ownerId;
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.isCurrentUser(#userId)")
    @GetMapping("/all/users/{user_id}")
    public String getAll(@PathVariable("user_id") long userId, Model model) {
        List<ToDo> todos = todoService.getByUserId(userId);
        model.addAttribute("todos", todos);
        model.addAttribute("user", userService.readById(userId));
        return "read-user";
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.isOwnerOfToDo(#id)")
    @GetMapping("/{id}/add")
    public String addCollaborator(@PathVariable long id, @RequestParam("user_id") long userId) {
        ToDo todo = todoService.readById(id);
        List<User> collaborators = todo.getCollaborators();
        collaborators.add(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        return "redirect:/todos/" + id + "/read";
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.isOwnerOfToDo(#id)")
    @GetMapping("/{id}/remove")
    public String removeCollaborator(@PathVariable long id, @RequestParam("user_id") long userId) {
        ToDo todo = todoService.readById(id);
        List<User> collaborators = todo.getCollaborators();
        collaborators.remove(userService.readById(userId));
        todo.setCollaborators(collaborators);
        todoService.update(todo);
        return "redirect:/todos/" + id + "/read";
    }

    // Auxiliary method to be used in authorization rules
    public boolean canReadToDo(long todoId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userService.findByUsername(username).orElseThrow();

        ToDo todo = todoService.readById(todoId);

        boolean isCollaborator = todo.getCollaborators()
                .stream()
                .anyMatch(collaborator -> collaborator.getId() == user.getId());


        return user.getId() == todo.getOwner().getId() || isCollaborator;

    }

}