package net.xaviersala.controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PrincipalController {
	
	@RequestMapping("/")
	public String arrel(Principal principal, @RequestParam(value = "error", required=false) boolean error, Model model) {
		Map<String, String> map = new LinkedHashMap<>();
		if (error) {
			model.addAttribute("error", true);
		}
		
		if (principal!=null) {
			
			model.addAttribute("name",principal.getName());
		} else {
			model.addAttribute("name","anònim");
		}
		return "index";
	}
	
	@RequestMapping("/unauthenticated")
	public String unauthenticated() {
	  return "redirect:/?error=true";
	}

}
