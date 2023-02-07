package com.cos.security1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cos.security1.config.auth.PrincipalDetails;
import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;

@Controller // View를 리턴하겠다.
public class IndexController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@GetMapping("/test/login")
	public @ResponseBody String testLogin(
			Authentication authentication //1번방법  //DI(의존성 주입)
			//, @AuthenticationPrincipal UserDetails userDetails
			, @AuthenticationPrincipal PrincipalDetails userDetails //PrincipalDetails에 UserDetails을 상속받았기 때문에 //2번방법
			) {  //DI(의존성 주입)
		System.out.println("/test/login=================================");
		PrincipalDetails principalDetails = (PrincipalDetails)authentication.getPrincipal(); //1번방법 다운캐스팅 과정 거쳐서
		System.out.println("authentication : "+principalDetails.getUser());  //User정보 받아오기 1번방법
		System.out.println("authentication : "+userDetails.getUser()); //User정보 받아오기 2번방법
		return "세션 정보 확인하기";
	}
	
	@GetMapping("/test/oauth/login")
	public @ResponseBody String testOauthLogin(
			Authentication authentication
			, @AuthenticationPrincipal OAuth2User oauth
			) {   //DI(의존성 주입)
		System.out.println("/test/oauth/login=================================");
		OAuth2User oauth2User = (OAuth2User)authentication.getPrincipal();
		System.out.println("authentication : "+oauth2User.getAttributes()); 
		System.out.println("oauth2User" + oauth.getAttributes());
		return "OAuth 세션 정보 확인하기";
	}
	
	//localhost:8090
	@GetMapping({"","/"})
	public String index() {
		//머스테치 기본폴더 src/main/resources/
		//뷰리절브 설정 : templates (prefix), mastache (suffix) 생략 가능!!
		return "index";  // src/main/resources/templates/index.mustache를 찾음
	}

	// OAuth 로그인을 해도 PrincipalDetails로 유저정보를 받을수 있고 
	// 일반 로그인을 해도 PrincipalDetails로 유저정보를 받을수 있다.
	@GetMapping("/user")
	public @ResponseBody String user(@AuthenticationPrincipal PrincipalDetails principalDetails) {
		System.out.println("PrincipalDetails : " + principalDetails.getUser());
		return "user";
	}
	
	@GetMapping("/admin")
	public @ResponseBody String admin() {
		return "admin";
	}
	
	@GetMapping("/manager")
	public @ResponseBody String manager() {
		return "manager";
	}
	
	// 스프링시큐리티가 해당주소를 낚아채버림 - SecurityConfig 파일 생성 후 작동 안함
	@GetMapping("/loginForm")
	public String login() {
		return "loginForm";
	}
	
	@GetMapping("/joinForm")
	public String joinForm() {
		return "joinForm";
	}
	
	@PostMapping("/join")
	public @ResponseBody String join(User user) {
		System.out.println(user);
		user.setRole("ROLE_USER");
		String rawPassword = user.getPassword();
		String encPassword = bCryptPasswordEncoder.encode(rawPassword);
		user.setPassword(encPassword);
		userRepository.save(user); //회원가입 잘됨, 비밀번호1234 => 시큐리티로 로그인을 할 수 없음. 암호화가 되어있지 않기때문에
		return "redirect:/loginForm";
	}
	
	@Secured("ROLE_ADMIN")
	@GetMapping("/info")
	public @ResponseBody String info() {
		return "개인정보";
	}
	
	@PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
	@GetMapping("/data")
	public @ResponseBody String data() {
		return "데이터정보";
	}
}
