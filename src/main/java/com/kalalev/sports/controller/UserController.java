package com.kalalev.sports.controller;

import jakarta.validation.Valid;
import com.kalalev.sports.model.User;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.kalalev.sports.service.UserService;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("")
    public String listUsers(Model model) {
        List<User> userList = userService.getAllUsers();
        model.addAttribute("userList", userList);
        return "user/list";
    }

    @GetMapping("/new")
    public String showCreateUserForm(Model model) {
        User user = new User();
        model.addAttribute("user", user);
        return "user/create";
    }

    @PostMapping("/new")
    public String createUser(@Valid User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "user/create";
        } else {
            userService.createUser(user);
            return "redirect:/users";
        }
    }

    @GetMapping("/{userId}/edit")
    public String showEditUserForm(@PathVariable("userId") int userId, Model model) {
        User user = userService.getUserById(userId);
        model.addAttribute("user", user);
        return "user/edit";
    }

    @PostMapping("/{userId}/edit")
    public String updateUser(@PathVariable("userId") int userId, @Valid User user,
                             BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "user/edit";
        } else {
            user.setUserId(userId);
            userService.updateUser(user);
            return "redirect:/users";
        }
    }

    @GetMapping("/{userId}/delete")
    public String deleteUser(@PathVariable("userId") int userId) {
        userService.deleteUserById(userId);
        return "redirect:/users";
    }
    @PostMapping("/{userId}/delete")
    public String delete(@PathVariable("userId") int userId) {
        userService.deleteUserById(userId);
        return "redirect:/users";
    }
}

