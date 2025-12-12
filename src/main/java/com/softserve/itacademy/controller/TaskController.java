package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.TaskDto;
import com.softserve.itacademy.model.TaskPriority;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.dto.TaskTransformer;
import com.softserve.itacademy.model.Task;
import com.softserve.itacademy.service.StateService;
import com.softserve.itacademy.service.ToDoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final ToDoService todoService;
    private final StateService stateService;
    private final TaskTransformer taskTransformer;

    @PreAuthorize("hasRole('ADMIN') or @authz.canReadToDo(#todoId)")
    @GetMapping("/create/todos/{todo_id}")
    public String create(@PathVariable("todo_id") long todoId, Model model) {
        model.addAttribute("task", new TaskDto());
        model.addAttribute("todo", todoService.readById(todoId));
        model.addAttribute("priorities", TaskPriority.values());
        return "create-task";
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.canReadToDo(#todoId)")
    @PostMapping("/create/todos/{todo_id}")
    public String create(@PathVariable("todo_id") long todoId, Model model,
                         @Validated @ModelAttribute("task") TaskDto taskDto, BindingResult result) {
        if (result.hasErrors()) {
            model.addAttribute("todo", todoService.readById(todoId));
            model.addAttribute("priorities", TaskPriority.values());
            return "create-task";
        }

        taskService.create(taskDto);
        log.info("Task was created");

        return "redirect:/todos/" + todoId + "/read";
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.canReadToDo(#todoId)")
    @GetMapping("/{task_id}/update/todos/{todo_id}")
    public String taskUpdateForm(@PathVariable("task_id") long taskId, @PathVariable("todo_id") long todoId, Model model) {
        TaskDto taskDto = taskTransformer.convertToDto(taskService.readById(taskId));
        model.addAttribute("task", taskDto);
        model.addAttribute("priorities", TaskPriority.values());
        model.addAttribute("states", stateService.getAll());
        return "update-task";
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.canReadToDo(#todoId)")
    @PostMapping("/{task_id}/update/todos/{todo_id}")
    public String update(@PathVariable("task_id") long taskId, @PathVariable("todo_id") long todoId, Model model,
                         @Validated @ModelAttribute("task") TaskDto taskDto, BindingResult result) {
        if (result.hasErrors()) {
            model.addAttribute("priorities", TaskPriority.values());
            model.addAttribute("states", stateService.getAll());
            return "update-task";
        }
        Task task = taskTransformer.fillEntityFields(
                new Task(),
                taskDto,
                todoService.readById(taskDto.getTodoId()),
                stateService.readById(taskDto.getStateId())
        );
        taskService.update(task);
        log.info("Task was updated");
        return "redirect:/todos/" + todoId + "/read";
    }

    @PreAuthorize("hasRole('ADMIN') or @authz.canReadToDo(#todoId)")
    @GetMapping("/{task_id}/delete/todos/{todo_id}")
    public String delete(@PathVariable("task_id") long taskId, @PathVariable("todo_id") long todoId) {
        taskService.delete(taskId);
        log.info("Task was deleted");
        return "redirect:/todos/" + todoId + "/read";
    }
}