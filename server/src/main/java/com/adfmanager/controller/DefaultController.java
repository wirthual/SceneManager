package com.adfmanager.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.adfmanager.domain.AdfDescription;
import com.adfmanager.service.AdfDescriptionService;

@Controller
public class DefaultController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultController.class);
	private final AdfDescriptionService service;

	
	@Autowired
	public DefaultController(AdfDescriptionService service) {
		this.service = service;
	}

	
	// GET /: Delivers landing page for login
	@RequestMapping(method = RequestMethod.GET,value={"", "/", "index"})
	public String provideLandingPage(Model model) throws IOException {
		return "index";
	}
	
	// GET /: Delivers landing page for login
	@RequestMapping(method = RequestMethod.GET,value="/about")
	public String provideAboutPage(Model model) throws IOException {
		return "about";
	}
	
	// GET /upload: Delivers form to upload file
	@RequestMapping(method = RequestMethod.GET, value = "/upload")
	public String provideUploadPage(Model model) throws IOException {
		return "upload";
	}
	
	// GET /upload: Delivers form to upload file
	@RequestMapping(method = RequestMethod.GET, value = "/manage")
	public String provideManagePage(Model model) throws IOException {
		return "manage";
	}
	
	// GET /: Delivers landing page for login
	@RequestMapping(method = RequestMethod.GET,value="/fordeveloper")
	public String provideDeveloperPage(Model model) throws IOException {
		return "fordeveloper";
	}
	
	// GET /upload: Delivers form to upload file
	@RequestMapping(method = RequestMethod.GET, value = "/overview")
	public String provideOverviewPage(Model model) throws IOException {
		List<AdfDescription> entries = service.getList();
		if (entries.isEmpty()) {
			model.addAttribute("filterInfo", "Filter Result: No files found.");
		} else {
			model.addAttribute("files", entries);
		}
		return "overview";
	}
}
