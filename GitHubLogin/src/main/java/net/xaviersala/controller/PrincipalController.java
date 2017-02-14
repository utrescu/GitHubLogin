package net.xaviersala.controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PrincipalController {
	
	@RequestMapping("/")
	public String arrel(Principal principal, Model model) {
		Map<String, String> map = new LinkedHashMap<>();
		if (principal!=null) {
			model.addAttribute("name",principal.getName());
		} else {
			model.addAttribute("name","anònim");
		}
		return "index";
	}

}
