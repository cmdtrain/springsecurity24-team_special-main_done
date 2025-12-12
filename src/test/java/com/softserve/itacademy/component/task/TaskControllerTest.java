package com.softserve.itacademy.component.task;

import com.softserve.itacademy.service.UserService;
import com.softserve.itacademy.controller.AccessDeniedController;
import com.softserve.itacademy.config.security.SecurityConfig;
import com.softserve.itacademy.config.security.AuthorizationService;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.config.SpringSecurityTestConfiguration;
import com.softserve.itacademy.config.WithMockCustomUser;
import com.softserve.itacademy.controller.TaskController;
import com.softserve.itacademy.dto.TaskDto;
import com.softserve.itacademy.dto.TaskTransformer;
import com.softserve.itacademy.model.*;
import com.softserve.itacademy.service.StateService;
import com.softserve.itacademy.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {
        TaskController.class,
        AccessDeniedController.class,
        SecurityConfig.class,
        SpringSecurityTestConfiguration.class,
        AuthorizationService.class,
        TaskTransformer.class })
@EnableMethodSecurity
public class TaskControllerTest {

    @MockBean private TaskService taskService;
    @MockBean private ToDoService todoService;
    @MockBean private StateService stateService;
    @MockBean private UserService userService;

    @Autowired
    private MockMvc mvc;

    private final TaskTransformer taskTransformer = new TaskTransformer();

    @Test
    @WithMockCustomUser(email = "mike@mail.com", role = UserRole.ADMIN)
    public void testCreateGetMethod() throws Exception {

        ToDo todo = new ToDo();
        todo.setId(1L);

        when(todoService.readById(anyLong())).thenReturn(todo);

        mvc.perform(get("/tasks/create/todos/1")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("create-task"))
                .andExpect(model().size(3))
                .andExpect(model().attribute("task", new TaskDto()))
                .andExpect(model().attribute("todo", todo))
                .andExpect(model().attribute("priorities", TaskPriority.values()))
                .andDo(print());

        verify(todoService).readById(anyLong());
        verifyNoMoreInteractions(todoService);
    }

    @Test
    @WithMockCustomUser(id = 2, email = "nick@mail.com", role = UserRole.USER)
    public void userCannotCreateTaskForForeignTodo() throws Exception {

        User owner = new User();
        owner.setId(4L);

        User userWithRoleUser = new User();
        userWithRoleUser.setId(2L);
        userWithRoleUser.setEmail("nick@mail.com");
        userWithRoleUser.setRole(UserRole.USER);

        ToDo todo = new ToDo();
        todo.setId(1L);
        todo.setOwner(owner);

        when(userService.readById(2L)).thenReturn(userWithRoleUser);
        when(todoService.readById(1L)).thenReturn(todo);

        mvc.perform(get("/tasks/create/todos/1")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden())
                .andExpect(view().name("access-denied"))
                .andDo(print());

        verify(todoService).readById(1L);
        verify(userService).readById(2L);
        verifyNoMoreInteractions(todoService, userService);
    }
}