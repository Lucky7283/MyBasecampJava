package com.mybasecamp.controller;

import com.mybasecamp.model.entity.Project;
import com.mybasecamp.model.entity.User;
import com.mybasecamp.model.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class AppController {

    private final UserService userService;
    private final ProjectService projectService;
    private final AttachmentService attachmentService;
    private final ThreadService threadService;
    private final MessageService messageService;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/src/main/resources/uploads";
    private final SessionService sessionService;

    public AppController(UserService userService, ProjectService projectService, AttachmentService attachmentService,
                         ThreadService threadService, MessageService messageService, SessionService sessionService) {
        this.userService = userService;
        this.projectService = projectService;
        this.attachmentService = attachmentService;
        this.threadService = threadService;
        this.messageService = messageService;
        this.sessionService = sessionService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/admin")
    public String admin(HttpServletRequest request, Model model) {
        User currentUser = (User) request.getAttribute("user");

        if (currentUser == null || currentUser.getAdmin() != 1) {
            return "redirect:/login";
        }

        Map<String, Object> result = userService.getAllUsersService();
        List<Map<String, Object>> users = List.of();
        long adminCount = 0;

        if ((Boolean) result.get("success")) {
            users = (List<Map<String, Object>>) result.get("data");
            if (users != null) {
                adminCount = users.stream()
                        .filter(u -> Boolean.TRUE.equals(u.get("admin")))
                        .count();
            }
        }

        model.addAttribute("users", users);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("user", currentUser);
        return "admin";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("error", null);
        return "register";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, @RequestParam(required = false) String email,
                        Model model) {
        model.addAttribute("error", error);
        model.addAttribute("email", email != null ? email : "");
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, Model model) {
        User user = (User) request.getAttribute("user");
        try {
            List<?> projectsData = user.getAdmin() == 1
                    ? (List<?>) projectService.getAllProjectsService().get("data")
                    : (List<?>) projectService.getUserProjectsService(user.getId()).get("data");
            if (projectsData == null)
                projectsData = List.of();

            var result = new HashMap<>();
            result.put("userName", user.getName() != null ? user.getName() : "User");
            result.put("userEmail", user.getEmail() != null ? user.getEmail() : "");
            result.put("userRole", user.getAdmin() == 1 ? "admin" : "user");
            result.put("isAdmin", user.getAdmin() == 1);
            result.put("userId", user.getId());
            result.put("projectsCount", projectsData.size());
            result.put("usersCount", 0);
            result.put("avatar", user.getName() != null && !user.getName().isEmpty() ? user.getName().charAt(0) : 'U');

            model.addAttribute("result", result);
            model.addAttribute("projects", projectsData);
            model.addAttribute("user", user);
        } catch (Exception e) {
            return "redirect:/login?error=Dashboard_error";
        }
        return "dashboard";
    }

    @PostMapping("/users")
    public String createUser(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam("password_confirm") String passwordConfirm,
                             Model model) {
        Map<String, Object> result = userService.createUser(name, email, password, passwordConfirm);
        if ((Boolean) result.get("success")) {
            return "redirect:/login";
        }
        model.addAttribute("error", result.get("message"));
        return "register";
    }

    @GetMapping("/users/{id}")
    public String getUser(@PathVariable Integer id, Model model) {
        Map<String, Object> result = userService.getUserById(id);
        if ((Boolean) result.get("success")) {
            model.addAttribute("user", result.get("data"));
            return "user";
        }
        return "projects";
    }

    @GetMapping("/users")
    public String listUsers() {
        return "redirect:/admin";
    }

    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/make_admin")
    public String makeAdmin(@PathVariable Integer id) {
        userService.makeUserAdmin(id);
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/remove_admin")
    public String removeAdmin(@PathVariable Integer id) {
        userService.removeUserAdminService(id);
        return "redirect:/admin";
    }

    @PostMapping("/sessions")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        @RequestParam(required = false) Boolean remember,
                        HttpServletResponse response,
                        Model model) {
        Map<String, Object> result = userService.loginUser(email, password, remember);
        if ((Boolean) result.get("success")) {
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String token = (String) data.get("token");
            jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            if (remember != null && remember) {
                cookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
            } else {
                cookie.setMaxAge(-1); // session cookie
            }
            response.addCookie(cookie);
            return "redirect:/dashboard";
        }
        model.addAttribute("error", result.get("message"));
        model.addAttribute("email", email);
        return "login";
    }

    @DeleteMapping("/sessions")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        String token = (String) request.getAttribute("token");
        if (token != null) {
            userService.logoutUser(token);
        }
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/login";
    }

    private boolean isOwnerOrAdmin(HttpServletRequest request, Integer projectId) {
        User user = (User) request.getAttribute("user");
        if (user == null)
            return false;
        if (user.getAdmin() == 1)
            return true;
        Project project = projectService.getProjectService(projectId).get("data") instanceof Project p ? p : null;
        if (project == null)
            return false;
        return project.getOwner().getId().equals(user.getId());
    }

    @GetMapping("/projects/new")
    public String newProject() {
        return "projects/new";
    }

    @GetMapping("/projects/{id}/edit")
    public String editProject(@PathVariable Integer id, HttpServletRequest request, Model model) {
        if (!isOwnerOrAdmin(request, id)) {
            return "error";
        }
        Map<String, Object> result = projectService.getProjectService(id);
        if ((Boolean) result.get("success")) {
            model.addAttribute("project", result.get("data"));
        }
        model.addAttribute("error", null);
        return "projects/edit";
    }

    @PostMapping("/projects")
    public String createProject(@RequestParam String name, @RequestParam(required = false) String description,
                                HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        String ownerIdParam = request.getParameter("owner_id");
        Integer finalOwnerId = ownerIdParam != null && !ownerIdParam.isBlank() ? Integer.parseInt(ownerIdParam)
                : user.getId();
        Map<String, Object> result = projectService.createProjectService(name, description, finalOwnerId);
        if ((Boolean) result.get("success")) {
            return "redirect:/dashboard?success=created";
        }
        return "projects/new";
    }

    @GetMapping("/projects")
    public String listProjects(Model model) {
        Map<String, Object> result = projectService.getAllProjectsService();
        if ((Boolean) result.get("success")) {
            model.addAttribute("projects", result.get("data"));
        }
        return "projects";
    }

    @GetMapping("/projects/{id}")
    public String getProject(@PathVariable Integer id, Model model) {
        Map<String, Object> result = projectService.getProjectService(id);
        if ((Boolean) result.get("success")) {
            model.addAttribute("project", result.get("data"));
            model.addAttribute("projects", List.of(result.get("data")));
        }
        return "projects";
    }

    @GetMapping("/projects/{id}/show")
    public String showProject(@PathVariable Integer id, HttpServletRequest request, Model model) {
        User user = (User) request.getAttribute("user");
        Map<String, Object> projectResult = projectService.getProjectService(id);
        if (!(Boolean) projectResult.get("success")) {
            return "projects";
        }
        Project project = (Project) projectResult.get("data");
        boolean isAdmin = project.getOwnerId().equals(user.getId());

        var attachmentsResult = attachmentService.getProjectAttachmentsService(id);
        var threadsResult = threadService.getProjectThreadsService(id);

        model.addAttribute("project", project);
        model.addAttribute("attachments", attachmentsResult.get("data"));
        model.addAttribute("threads", threadsResult.get("data"));
        model.addAttribute("user", user);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("error", request.getParameter("error"));
        return "projects/show";
    }

    @PutMapping("/projects/{id}")
    public String updateProject(@PathVariable Integer id, @RequestParam String name,
                                @RequestParam(required = false) String description, HttpServletRequest request, Model model) {
        if (!isOwnerOrAdmin(request, id)) {
            return "error";
        }
        Map<String, Object> result = projectService.updateProjectService(id, name, description);
        if ((Boolean) result.get("success")) {
            return "redirect:/dashboard?success=updated";
        }
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setDescription(description);
        model.addAttribute("project", project);
        model.addAttribute("error", result.get("message"));
        return "projects/edit";
    }

    @DeleteMapping("/projects/{id}")
    public String deleteProject(@PathVariable Integer id, HttpServletRequest request) {
        if (!isOwnerOrAdmin(request, id)) {
            return "error";
        }
        projectService.deleteProjectService(id);
        return "redirect:/dashboard?success=deleted";
    }

    @GetMapping(value = "/users/{userId}/projects")
    public String userProjects(@PathVariable Integer userId, Model model) {
        Map<String, Object> result = projectService.getUserProjectsService(userId);
        if ((Boolean) result.get("success")) {
            model.addAttribute("projects", result.get("data"));
        }
        return "projects";
    }

    @PostMapping(value = "/projects/{projectId}/attachments", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createAttachment(@PathVariable Integer projectId, @RequestParam("file") MultipartFile file,
                                   HttpServletRequest request) throws IOException {
        User user = (User) request.getAttribute("user");
        Map<String, Object> projectResult = projectService.getProjectService(projectId);
        if (!(Boolean) projectResult.get("success")) {
            return "error";
        }
        Project project = (Project) projectResult.get("data");
        if (!project.getOwnerId().equals(user.getId())) {
            return "error";
        }

        if (file.isEmpty()) {
            return "redirect:/projects/" + projectId + "/show?error=no_file";
        }

        String originalFilename = file.getOriginalFilename();
        String ext = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String storedFilename = UUID.randomUUID().toString() + ext;
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        file.transferTo(uploadPath.resolve(storedFilename).toFile());

        attachmentService.createAttachmentService(projectId, storedFilename, originalFilename, file.getContentType(),
                file.getSize());
        return "redirect:/projects/" + projectId + "/show";
    }

    @DeleteMapping("/projects/{projectId}/attachments/{attachmentId}")
    public String deleteAttachment(@PathVariable Integer projectId, @PathVariable Integer attachmentId,
                                   HttpServletRequest request) throws IOException {
        User user = (User) request.getAttribute("user");
        Map<String, Object> projectResult = projectService.getProjectService(projectId);
        if (!(Boolean) projectResult.get("success")) {
            return "error";
        }
        Project project = (Project) projectResult.get("data");
        if (!project.getOwnerId().equals(user.getId())) {
            return "error";
        }

        Map<String, Object> attachmentResult = attachmentService.getAttachmentService(attachmentId);
        if ((Boolean) attachmentResult.get("success")) {
            com.mybasecamp.model.entity.Attachment attachment = (com.mybasecamp.model.entity.Attachment) attachmentResult
                    .get("data");
            Path filePath = Paths.get(UPLOAD_DIR, attachment.getFilename());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        }
        attachmentService.deleteAttachmentService(attachmentId);
        return "redirect:/projects/" + projectId + "/show";
    }

    @GetMapping("/uploads/{filename}")
    public ResponseEntity<Resource> showAttachment(@PathVariable String filename, HttpServletRequest request) throws FileNotFoundException {
        File file = new File(UPLOAD_DIR, filename);
        Resource resource = new FileSystemResource(file);
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);


    }

    @GetMapping("/projects/{projectId}/threads/new")
    public String newThread(@PathVariable Integer projectId, HttpServletRequest request, Model model) {
        User user = (User) request.getAttribute("user");
        Map<String, Object> projectResult = projectService.getProjectService(projectId);
        if (!(Boolean) projectResult.get("success")) {
            return "error";
        }
        Project project = (Project) projectResult.get("data");
        if (!project.getOwnerId().equals(user.getId())) {
            return "error";
        }
        model.addAttribute("project", project);
        model.addAttribute("error", null);
        return "threads/new";
    }

    @PostMapping("/projects/{projectId}/threads")
    public String createThread(@PathVariable Integer projectId, @RequestParam String title, HttpServletRequest request,
                               Model model) {
        User user = (User) request.getAttribute("user");
        Map<String, Object> projectResult = projectService.getProjectService(projectId);
        if (!(Boolean) projectResult.get("success")) {
            return "error";
        }
        Project project = (Project) projectResult.get("data");
        if (!project.getOwnerId().equals(user.getId())) {
            return "error";
        }

        if (title == null || title.isBlank()) {
            model.addAttribute("project", project);
            model.addAttribute("error", "Title is required.");
            return "threads/new";
        }

        Map<String, Object> result = threadService.createThreadService(projectId, title.trim(), user.getId());
        if (!(Boolean) result.get("success")) {
            model.addAttribute("project", project);
            model.addAttribute("error", result.get("message"));
            return "threads/new";
        }
        Integer threadId = ((com.mybasecamp.model.entity.Thread) result.get("data")).getId();
        return "redirect:/projects/" + projectId + "/threads/" + threadId;
    }

    @GetMapping("/projects/{projectId}/threads/{threadId}/edit")
    public String editThread(@PathVariable Integer projectId, @PathVariable Integer threadId,
                             HttpServletRequest request, Model model) {
        User user = (User) request.getAttribute("user");
        Map<String, Object> projectResult = projectService.getProjectService(projectId);
        if (!(Boolean) projectResult.get("success")) {
            return "error";
        }
        Project project = (Project) projectResult.get("data");
        if (!project.getOwnerId().equals(user.getId())) {
            return "error";
        }

        Map<String, Object> threadResult = threadService.getThreadService(threadId);
        if (!(Boolean) threadResult.get("success")) {
            return "error";
        }
        model.addAttribute("thread", threadResult.get("data"));
        model.addAttribute("project", project);
        model.addAttribute("error", null);
        return "threads/edit";
    }

    @PutMapping("/projects/{projectId}/threads/{threadId}")
    public String updateThread(@PathVariable Integer projectId, @PathVariable Integer threadId,
                               @RequestParam String title, HttpServletRequest request, Model model) {
        User user = (User) request.getAttribute("user");
        Map<String, Object> projectResult = projectService.getProjectService(projectId);
        if (!(Boolean) projectResult.get("success")) {
            return "error";
        }
        Project project = (Project) projectResult.get("data");
        if (!project.getOwnerId().equals(user.getId())) {
            return "error";
        }

        if (title == null || title.isBlank()) {
            Map<String, Object> threadResult = threadService.getThreadService(threadId);
            model.addAttribute("thread", threadResult.get("data"));
            model.addAttribute("project", project);
            model.addAttribute("error", "Title is required.");
            return "threads/edit";
        }

        threadService.updateThreadService(threadId, title.trim());
        return "redirect:/projects/" + projectId + "/threads/" + threadId;
    }

    @DeleteMapping("/projects/{projectId}/threads/{threadId}")
    public String deleteThread(@PathVariable Integer projectId, @PathVariable Integer threadId,
                               HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        Map<String, Object> projectResult = projectService.getProjectService(projectId);
        if (!(Boolean) projectResult.get("success")) {
            return "error";
        }
        Project project = (Project) projectResult.get("data");
        if (!project.getOwnerId().equals(user.getId())) {
            return "error";
        }
        threadService.deleteThreadService(threadId);
        return "redirect:/projects/" + projectId + "/show";
    }

    @GetMapping("/projects/{projectId}/threads/{threadId}")
    public String showThread(@PathVariable Integer projectId, @PathVariable Integer threadId,
                             HttpServletRequest request, Model model) {
        User user = (User) request.getAttribute("user");
        Map<String, Object> threadResult = threadService.getThreadService(threadId);
        if (!(Boolean) threadResult.get("success")) {
            return "error";
        }
        Map<String, Object> projectResult = projectService.getProjectService(projectId);
        Project project = (Project) projectResult.get("data");
        boolean isAdmin = project.getOwnerId().equals(user.getId());

        model.addAttribute("thread", threadResult.get("data"));
        model.addAttribute("project", project);
        model.addAttribute("user", user);
        model.addAttribute("isAdmin", isAdmin);
        return "threads/show";
    }

    @PostMapping("/projects/{projectId}/threads/{threadId}/messages")
    public String createMessage(@PathVariable Integer projectId, @PathVariable Integer threadId,
                                @RequestParam String content, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        Map<String, Object> projectResult = projectService.getProjectService(projectId);
        if (!(Boolean) projectResult.get("success")) {
            return "error";
        }
        Project project = (Project) projectResult.get("data");
        if (!project.getOwnerId().equals(user.getId())) {
            return "error";
        }

        if (content == null || content.isBlank()) {
            return "redirect:/projects/" + projectId + "/threads/" + threadId + "?error=empty";
        }
        messageService.createMessageService(threadId, user.getId(), content.trim());
        return "redirect:/projects/" + projectId + "/threads/" + threadId;
    }

    @PutMapping("/projects/{projectId}/threads/{threadId}/messages/{messageId}")
    public String updateMessage(@PathVariable Integer projectId, @PathVariable Integer threadId,
                                @PathVariable Integer messageId, @RequestParam String content, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        Map<String, Object> messageResult = messageService.getMessageService(messageId);
        if (!(Boolean) messageResult.get("success")) {
            return "error";
        }
        com.mybasecamp.model.entity.Message msg = (com.mybasecamp.model.entity.Message) messageResult.get("data");

        Map<String, Object> projectResult = projectService.getProjectService(projectId);
        Project project = (Project) projectResult.get("data");
        boolean admin = project.getOwnerId().equals(user.getId());

        if (!msg.getAuthorId().equals(user.getId()) && !admin) {
            return "error";
        }

        if (content == null || content.isBlank()) {
            return "redirect:/projects/" + projectId + "/threads/" + threadId + "?error=empty";
        }
        messageService.updateMessageService(messageId, content.trim());
        return "redirect:/projects/" + projectId + "/threads/" + threadId;
    }

    @DeleteMapping("/projects/{projectId}/threads/{threadId}/messages/{messageId}")
    public String deleteMessage(@PathVariable Integer projectId, @PathVariable Integer threadId,
                                @PathVariable Integer messageId, HttpServletRequest request) {
        User user = (User) request.getAttribute("user");
        Map<String, Object> messageResult = messageService.getMessageService(messageId);
        if (!(Boolean) messageResult.get("success")) {
            return "error";
        }
        com.mybasecamp.model.entity.Message msg = (com.mybasecamp.model.entity.Message) messageResult.get("data");

        Map<String, Object> projectResult = projectService.getProjectService(projectId);
        Project project = (Project) projectResult.get("data");
        boolean admin = project.getOwnerId().equals(user.getId());

        if (!msg.getAuthorId().equals(user.getId()) && !admin) {
            return "error";
        }
        messageService.deleteMessageService(messageId);
        return "redirect:/projects/" + projectId + "/threads/" + threadId;
    }
}
