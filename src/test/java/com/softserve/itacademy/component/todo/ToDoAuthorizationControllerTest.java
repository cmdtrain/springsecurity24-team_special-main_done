package com.softserve.itacademy.component.todo;

import com.softserve.itacademy.config.SpringSecurityTestConfiguration;
import com.softserve.itacademy.config.WithMockCustomUser;
import com.softserve.itacademy.config.security.AuthorizationService;
import com.softserve.itacademy.config.security.SecurityConfig;
import com.softserve.itacademy.controller.AccessDeniedController;
import com.softserve.itacademy.controller.ToDoController;
import com.softserve.itacademy.model.ToDo;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.service.TaskService;
import com.softserve.itacademy.service.ToDoService;
import com.softserve.itacademy.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {
        ToDoController.class,
        AccessDeniedController.class,
        SecurityConfig.class,
        SpringSecurityTestConfiguration.class,
        AuthorizationService.class
})
public class ToDoAuthorizationControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ToDoService todoService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserService userService;

    @Autowired
    @Qualifier("withRoleUser")
    private User userWithRoleUser;

    @Test
    @WithMockCustomUser(id = 2, email = "nick@mail.com", role = UserRole.USER)
    public void userCanReadTodoAsCollaborator() throws Exception {
        User owner = new User();
        owner.setId(4L);

        ToDo todo = new ToDo();
        todo.setId(7L);
        todo.setOwner(owner);
        todo.setCollaborators(List.of(userWithRoleUser));

        when(userService.readById(2L)).thenReturn(userWithRoleUser);
        when(todoService.readById(7L)).thenReturn(todo);
        when(taskService.getByTodoId(7L)).thenReturn(List.of());

        mvc.perform(get("/todos/7/read").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("read-todo"))
                .andDo(print());

        verify(userService, times(1)).readById(2L);
        verify(todoService, times(1)).readById(7L);
    }

    @Test
    @WithMockCustomUser(id = 2, email = "nick@mail.com", role = UserRole.USER)
    public void userCannotReadForeignTodo() throws Exception {
        User owner = new User();
        owner.setId(4L);

        ToDo todo = new ToDo();
        todo.setId(10L);
        todo.setOwner(owner);
        todo.setCollaborators(List.of());

        when(userService.readById(2L)).thenReturn(userWithRoleUser);
        when(todoService.readById(10L)).thenReturn(todo);

        mvc.perform(get("/todos/10/read").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden())
                .andExpect(view().name("access-denied"))
                .andDo(print());

        verify(userService, times(1)).readById(2L);
        verify(todoService, times(1)).readById(10L);
    }

    @Test
    @WithMockCustomUser(id = 2, email = "nick@mail.com", role = UserRole.USER)
    public void userCannotOpenAnotherUsersTodoList() throws Exception {
        mvc.perform(get("/todos/all/users/4").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden())
                .andExpect(view().name("access-denied"))
                .andDo(print());

        verifyNoInteractions(todoService, taskService, userService);
    }

    @Test
    @WithMockCustomUser(id = 2, email = "nick@mail.com", role = UserRole.USER)
    public void userCannotAddCollaboratorIfNotOwner() throws Exception {
        User owner = new User();
        owner.setId(4L);

        ToDo todo = new ToDo();
        todo.setId(12L);
        todo.setOwner(owner);
        todo.setCollaborators(List.of());

        when(userService.readById(2L)).thenReturn(userWithRoleUser);
        when(todoService.readById(12L)).thenReturn(todo);

        mvc.perform(get("/todos/12/add").param("user_id", "4").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden())
                .andExpect(view().name("access-denied"))
                .andDo(print());

        verify(userService, times(1)).readById(2L);
        verify(todoService, times(1)).readById(12L);
    }
}
