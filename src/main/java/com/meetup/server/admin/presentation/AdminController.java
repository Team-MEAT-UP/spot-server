package com.meetup.server.admin.presentation;

import com.meetup.server.admin.application.AdminService;
import com.meetup.server.admin.dto.request.AdminLoginRequest;
import com.meetup.server.admin.dto.request.AdminRegisterRequest;
import com.meetup.server.admin.dto.response.*;
import com.meetup.server.admin.exception.AdminErrorType;
import com.meetup.server.admin.exception.AdminException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admins")
public class AdminController {

    @Value("${client-url}")
    private String clientUrl;

    private final AdminService adminService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("adminRegisterRequest", new AdminRegisterRequest("", "", "", ""));
        return "admin/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("adminRegisterRequest") AdminRegisterRequest adminRegisterRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "admin/register";
        }

        try {
            adminService.register(adminRegisterRequest);
        } catch (AdminException e) {
            if (e.getErrorType() == AdminErrorType.ADMIN_ALREADY_EXISTS) {
                bindingResult.rejectValue("username", "AdminAlreadyExists", e.getMessage());
            }
            return "admin/register";
        } catch (Exception e) {
            bindingResult.reject("InternalServerError", "회원가입 중 오류가 발생했습니다.");
        }
        return "redirect:/admins/login";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("adminLoginRequest", new AdminLoginRequest("", ""));
        return "admin/login";
    }

    @GetMapping("/events")
    public String getEvents(
            Model model,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer participantCount,
            @PageableDefault(size = 20, sort = "created_at", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminEventResponse> eventPage = adminService.getFilteredEvents(startDate, endDate, participantCount, pageable);
        Map<Integer, Long> eventCountByParticipantCount = adminService.getEventCountByParticipantCount(startDate, endDate);
        DailyEventStatsResponse dailyEventStats = adminService.getDailyEventStats();

        model.addAttribute("eventPage", eventPage);
        model.addAttribute("eventCountByParticipantCount", eventCountByParticipantCount);
        model.addAttribute("clientUrl", clientUrl);
        model.addAttribute("dailyEventStats", dailyEventStats);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("participantCount", participantCount);
        return "admin/events";
    }

    @GetMapping("/users")
    public String getUsers(
            Model model,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminUserResponse> userPage = adminService.getUsers(pageable);
        DailyUserStatsResponse dailyUserStats = adminService.getDailyUserStats();

        model.addAttribute("userPage", userPage);
        model.addAttribute("dailyUserStats", dailyUserStats);
        return "admin/users";
    }

    @GetMapping("/stats")
    public String getStats(
            Model model,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        PeriodStatsResponse stats = adminService.getPeriodStats(startDate, endDate);

        model.addAttribute("stats", stats);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "admin/stats";
    }
}
