Add to the Web application a Security Level, which includes user Authentication and Authorization.

Implemented security level should include the next scenarios:

1. If the role of the user is ADMIN, then allow him full access to all resources of the web application.

2. If the role of the user is USER, then:

After going to the “All To-Dos of …” page, display for user the list of To-Dos  where he is owner or collaborator. If user does not owner To-Do, then he isn't allowed  to delete or edit this To-Do.

User can view any To-Dos where he is owner or collaborator.

Prohibit for user add collaborators  to To-Do if he is not the owner of this To-Do.

In case of an attempt unauthorized access to forbidden URL, redirect the user to the “Access Denied” page and set the status code to 403.

3. If the user clicks the "LogOut" button, then end the user session and redirect him to the login page.




Introduce changes into  SecurityConfig.java

Search for the comments in the controllers and implement the described authorization rules:

// TODO: can create todo if is owner or collaborator
@GetMapping("/create/todos/{todo_id}")
public String create(@PathVariable("todo_id") long todoId, Model model) {...}

Look at the test examples and write your own tests that check your authorization rules

The user password should encoded using the BCrypt encryption algorithm.

*** Record a short video (5 -10 minutes) where demonstrate the functionality your Web Application and publish it in your YouTube channel.