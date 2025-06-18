package com.bigboss.offbeatHub.web;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.bigboss.offbeatHub.dto.ApiResponse;
import com.bigboss.offbeatHub.dto.UserDTO;
import com.bigboss.offbeatHub.service.MainService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Controller
@RequiredArgsConstructor
public class MainController {

    private final MainService mainService;

    @GetMapping("/")
    public String main(){
        mainService.test();
        return "main";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model model){
        model.addAttribute("user", new UserDTO());
        return "signup";
    }


    @PostMapping("/signup")
    public String signup(@ModelAttribute UserDTO user, BindingResult result) {
        // 회원가입 로직
        return "redirect:/login";
    }

    @GetMapping("/artwork")
    public ApiResponse<List<String>> getArtwork(){
        List<String> artwork = Arrays.asList("그림", "사진", "디자인", "일러스트");
        return ApiResponse.success(artwork, "성공");
    }

    @GetMapping("/pdf-converter")
    public String pdfConverter(){
        return "image_to_pdf";
    }
}
