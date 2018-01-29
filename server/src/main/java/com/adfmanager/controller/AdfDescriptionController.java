package com.adfmanager.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.adfmanager.domain.AdfDescription;
import com.adfmanager.service.AdfDescriptionService;

@RestController
@RequestMapping("/api/")
public class AdfDescriptionController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AdfDescriptionController.class);
	private final AdfDescriptionService service;

	@Inject
	public AdfDescriptionController(final AdfDescriptionService service) {
		this.service = service;
	}

	/**
	 * @api {get} /api/adf Request ADF Descriptions
	 * @apiName GetADFDescriptions
	 * @apiGroup ADF
	 *
	 *
	 * @apiSuccess {Object[]} ADF Descriptions List of available ADF Descriptions
	 * @apiSampleRequest http://localhost:8080/adf
	 * @apiSuccessExample {json} Success-Response:
	 * 		HTTP/1.1 200 OK 
	 * 		[
	 *		  {
	 *		    "id": 1,
	 *		    "lat": 49.00790579977143,
	 *		    "lng": 8.412201404571535,
	 *		    "lvl": 3,
	 *		    "name": "dfd",
	 *		    "fileName": "1509468537814_8aa864dc-6442-40de-b501-e572f1042378.zip",
	 *		    "description": "salkdjflkdsjflkdsajfalkdsjflkdsjfjdsfölksajdflksadjfadsgädsikvflkdsa",
	 *		    "uuid": "8aa864dc-6442-40de-b501-e572f1042378"
	 *		  }
	 *		]
	 * 
	 */
	@RequestMapping(value = "/adf", method = RequestMethod.GET)
	public List<AdfDescription> listAdfDescriptions() {
		LOGGER.debug("Received request to list all users");

		return service.getList();
	}
	
	
	/**
	 * @api {get} /api/adf/user Request ADF Descriptions from currently authenticated user
	 * @apiName GetADFDescriptions for current user
	 * @apiGroup ADF
	 *
	 *
	 * @apiSuccess {Object[]} ADF Descriptions List from user
	 * @apiSampleRequest http://localhost:8080/adf/user
	 * @apiSuccessExample {json} Success-Response:
	 * 		HTTP/1.1 200 OK 
	 * 		[
	 *		  {
	 *		    "id": 1,
	 *		    "lat": 49.00790579977143,
	 *		    "lng": 8.412201404571535,
	 *		    "lvl": 3,
	 *		    "name": "dfd",
	 *		    "fileName": "1509468537814_8aa864dc-6442-40de-b501-e572f1042378.zip",
	 *		    "description": "salkdjflkdsjflkdsajfalkdsjflkdsjfjdsfölksajdflksadjfadsgädsikvflkdsa",
	 *		    "uuid": "8aa864dc-6442-40de-b501-e572f1042378"
	 *		  }
	 *		]
	 * 
	 */
	@RequestMapping(value = "/adf/user", method = RequestMethod.GET)
	public List<AdfDescription> listAdfDescriptionsByUser() {
		LOGGER.debug("Received request to list all adfs from user");

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		OAuth2Authentication auth = (OAuth2Authentication) authentication;
		@SuppressWarnings("unchecked")
		Map<String, String> info = (Map<String, String>) auth.getUserAuthentication().getDetails();
		String userId = info.get("sub");

		return service.getListByUser(userId);
	}

	/**
	 * @api {get} /api/adf/:id Request ADF Descriptions by id
	 * @apiName GetADFDescription by id
	 * @apiGroup ADF
	 *
	 * @apiParam {Number} id Users unique ID.
	 *
	 * @apiSuccess {Number} id Unique ID of ADF
	 * @apiSuccess {Number} lat Latitude of location
	 * @apiSuccess {Number} lng Longitude of location
	 * @apiSuccess {Number} lvl Level e.g 1st floor in a building
	 * @apiSuccess {String} name Name of the ADF
	 * @apiSuccess {String} fileName File Name of the ADF
	 * @apiSuccess {String} description Description of the ADF
	 * @apiSuccess {String} uuid uuid of ADF, given from Tango service
	 *
	 * @apiSampleRequest http://localhost:8080/1
	 *
	 * @apiSuccessExample {json} Success-Response: 
	 * 		HTTP/1.1 200 OK 
	 *		{
  	 *		"id": 1,
  	 *		"lat": 49.00790579977143,
  	 *		"lng": 8.412201404571535,
  	 *		"lvl": 3,
  	 *		"name": "dfd",
  	 *		"fileName": "1509468537814_8aa864dc-6442-40de-b501-e572f1042378.zip",
     *		"description": "salkdjflkdsjflkdsajfalkdsjflkdsjfjdsfölksajdflksadjfadsgädsikvflkdsa",
  	 *		"uuid": "8aa864dc-6442-40de-b501-e572f1042378"
	 *		}
	 */
	@RequestMapping(value = "/adf/{id}", method = RequestMethod.GET)
	public AdfDescription getAdfDescriotion(@PathVariable("id") long id) {
		return service.getAdfFile(id);
	}

	/**
	 * @api {delete} /api/adf/:id Delete ADF Description
	 * @apiName DeleteADFDescriptions
	 * @apiGroup ADF
	 *
	 * @apiSuccess {boolean} True if deletion was successful, false otherwise
	 * @apiSuccessExample {boolean} Success-Response:
	 * 		HTTP/1.1 200 OK
	 * 		true
	 * 
	 * @apiSampleRequest http://localhost:8080/adf/1
	 */
	@RequestMapping(value = "/adf/{id}", method = RequestMethod.DELETE)
	public boolean delete(@PathVariable long id) {
		return service.delete(id);
	}

	/**
	 * @api {get} /api/adf Request ADF Descriptions near to given location
	 * @apiName GetADFDescriptions by location
	 * @apiSampleRequest http://localhost:8080/nearby?lat=49.012762&lng=8.424176&radius=100000.0
	 * @apiGroup ADF
	 * 
	 * @apiParam {Number} lat Latitude 
	 * @apiParam {Number} lng Longitude
	 * @apiParam {Number} radius Radius for area, depending on lat and lng to search in
	 * @apiParam {Number} [lvl] Level
	 * 
	 * @apiSuccess {Object[]} ADF Descriptions near location
	 * @apiSuccessExample {json} Success-Response:
	 * 		HTTP/1.1 200 OK 
	 * 		[
	 *		  {
	 *		    "id": 1,
	 *		    "lat": 49.00790579977143,
	 *		    "lng": 8.412201404571535,
	 *		    "lvl": 3,
	 *		    "name": "dfd",
	 *		    "fileName": "1509468537814_8aa864dc-6442-40de-b501-e572f1042378.zip",
	 *		    "description": "salkdjflkdsjflkdsajfalkdsjflkdsjfjdsfölksajdflksadjfadsgädsikvflkdsa",
	 *		    "uuid": "8aa864dc-6442-40de-b501-e572f1042378"
	 *		  }
	 *		]
	 * 
	 */
	@RequestMapping(value = "/nearby", method = RequestMethod.GET)
	public List<AdfDescription> listNearbyDescriptions(@RequestParam(value = "lng",required = true) double lng,
			@RequestParam(value = "lat", required = true) double lat,
			@RequestParam(value = "radius", required = true) double radius,
			@RequestParam(value = "lvl", required = false) Integer lvl) {
		return service.getNearbyList(lng, lat, radius, lvl);
	}
	
	/**  data.setForceMultipartEntityContentType(true);
	 * @api {get} /api/search Request ADF Descriptions based on description
	 * @apiName GetADFDescriptions by descripiton
	 * @apiSampleRequest http://localhost:8080/search?desc="findInDescriptions"
	 * @apiGroup ADF
	 * 
	 * @apiParam {String} desc String to search in the description of ADF Files
	 * 
	 * @apiSuccess {Object[]} ADF Descriptions including desc in their description
	 * @apiSuccessExample {json} Success-Response:
	 * 		HTTP/1.1 200 OK 
	 * 		[
	 *		  {
	 *		    "id": 1,
	 *		    "lat": 49.00790579977143,
	 *		    "lng": 8.412201404571535,
	 *		    "lvl": 3,
	 *		    "name": "dfd",
	 *		    "fileName": "1509468537814_8aa864dc-6442-40de-b501-e572f1042378.zip",
	 *		    "description": "salkdjflkdsjflkdsajfalkdsjflkdsjfjdsfölksajdflksadjfadsgädsikvflkdsa",
	 *		    "uuid": "8aa864dc-6442-40de-b501-e572f1042378"
	 *		  }
	 *		]
	 * 
	 */
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public List<AdfDescription> listContainTextDescriptions(
			@RequestParam(value = "desc", required=true) String desc) {
		return service.getListContains(desc);
	}
}
