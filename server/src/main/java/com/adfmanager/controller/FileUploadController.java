package com.adfmanager.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.adfmanager.AdfManagerApplication;
import com.adfmanager.domain.AdfDescription;
import com.adfmanager.service.AdfDescriptionService;

@Controller
@RequestMapping("/api/")
public class FileUploadController {

	private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

	public static final String TMP = "/tmp/SceneManager";

	public static final String MIME_TYPE_CHECK = "application/json";


	private final ResourceLoader resourceLoader;
	private final AdfDescriptionService service;

	@Autowired
	public FileUploadController(ResourceLoader resourceLoader, final AdfDescriptionService service) {
		this.resourceLoader = resourceLoader;
		this.service = service;
	}

	/**
	 * @api {get} /file/id/:id Get Scene by id
	 * @apiName Get Scene by id
	 * @apiGroup File 
	 * 
	 * @apiParam {Number} id Files unique ID.
	 * 
	 * */
	@RequestMapping(value = "file/id/{id}", method = RequestMethod.GET)
	public void getDownload(@PathVariable("id") long id, HttpServletResponse response) throws Exception {

		AdfDescription desc = service.getAdfFile(id);
		String fileName = desc.getFileName();
		String type = desc.getType();
		String path = "";
		if(type.equals(AdfDescription.TYPE_SCENE)) {
			path = Paths.get(AdfManagerApplication.STORAGE_SCENE, fileName).toString();
		}else if (type.equals(AdfDescription.TYPE_ADF)) {
			path = Paths.get(AdfManagerApplication.STORAGE_ADF, fileName).toString();
		}else {
			response.sendError(404);
		}

		InputStream fStream = new FileInputStream(path);

		response.addHeader("Content-disposition", "attachment;filename=" + desc.getFileName().substring(14));
		response.setContentType("txt/plain");

		IOUtils.copy(fStream, response.getOutputStream());
		response.flushBuffer();
	}

	/**
	 * @api {post} /file/upload/scene Upload scene by form. Form must include Name,Description,Latitude,Longitude,Level,File
	 * File needs to be a zip including ADF and Sqlite-Database
	 * @apiName File upload
	 * @apiGroup File 
	 * 
	 * @apiHeaderExample {String} Request-Example:
	 * 				Accept-Encoding: gzip, deflate
	 *				Content-Type: multipart/form-data; 
	 * */
	@RequestMapping(method = RequestMethod.POST, value = "/file/upload/scene")
	public String handleSceneUpload(@RequestParam("file") MultipartFile file, @RequestParam("lat") double lat,
			@RequestParam("lng") double lng, @RequestParam("desc") String desc, @RequestParam("name") String name,
			@RequestParam("lvl") int lvl,@RequestParam(value="alt",required=false,defaultValue="0.0") double alt,@RequestParam("type") String type,Principal principal, RedirectAttributes redirectAttributes) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		OAuth2Authentication auth = (OAuth2Authentication) authentication;
		Map<String, String> info = (Map<String, String>)auth.getUserAuthentication().getDetails();
		String userId = info.get("sub");
         
		String uuid = file.getOriginalFilename().split("\\.")[0];
		String fileName = String.valueOf(System.currentTimeMillis()) + "_" + file.getOriginalFilename();
		File adfDbZip = new File(AdfManagerApplication.STORAGE_SCENE, fileName);
		File tmpDir = new File(TMP, uuid);
		
		File tmpAdf = new File(tmpDir.getAbsolutePath(), uuid);
		File tmpJson = new File(tmpDir.getAbsolutePath(), uuid + ".json");

		try {
			Files.copy(file.getInputStream(), adfDbZip.toPath());

			// File was received: Now unzip
			unzip(adfDbZip.getAbsolutePath(), tmpDir.getAbsolutePath());

			// Check if exactly 2 Files are present
			File[] fl = tmpDir.listFiles();
			if (fl.length != 2) {
				throw new IOException("Uploaded file format is not supported");
			}
			
			if (!tmpAdf.exists()) { // Check if ADF File exists -- uuid is name
				throw new IOException("Missing ADF-File in ZIP file "+uuid);
			} else {
				boolean adfOk = checkAdfFile(tmpAdf,uuid);
				if(!adfOk) {
					throw new IOException("Invalid ADF-File "+uuid);
				}
			}
			if (!tmpJson.exists()) { // Check if DB File exists -- uuid is name with db suffix
				throw new IOException("Missing json file in ZIP file "+uuid);
			} else {
				String mimeType = Files.probeContentType(tmpJson.toPath());
				log.debug("Mime-Type of File in Zip:" + mimeType);
				if (!mimeType.equals(MIME_TYPE_CHECK)) { //Check if MIME type is sqlite3
					throw new IOException("Wrong file found in ZIP file "+uuid);
				}
			}
		} catch (IOException e) {
			//Clean-UP
			adfDbZip.delete();
			tmpAdf.delete();
			tmpJson.delete();
			tmpDir.delete();
			
			redirectAttributes.addFlashAttribute("message",
					"Failued to upload " + file.getOriginalFilename() + " => " + e.getMessage());

			return "redirect:/upload";
		}

		AdfDescription newDesc = new AdfDescription(lat, lng, lvl,alt, name, fileName, desc, uuid,userId,AdfDescription.TYPE_SCENE);
		service.save(newDesc);

		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");

		//Clean-UP temp files
		tmpAdf.delete();
		tmpJson.delete();
		tmpDir.delete();
		return "redirect:/manage";
	}
	
	/**
	 * @api {post} /file/upload/adf Upload adf by form. Form must include Name,Description,Latitude,Longitude,Level,File
	 * File needs to be a an adf file
	 * @apiName File upload
	 * @apiGroup File 
	 * 
	 * @apiHeaderExample {String} Request-Example:
	 * 				Accept-Encoding: gzip, deflate
	 *				Content-Type: multipart/form-data; 
	 * */
	@RequestMapping(method = RequestMethod.POST, value = "/file/upload/adf")
	public ResponseEntity<Object> handleAdfUpload(@RequestParam("file") MultipartFile file, @RequestParam("lat") double lat,
			@RequestParam("lng") double lng, @RequestParam("desc") String desc, @RequestParam("name") String name,
			@RequestParam("lvl") int lvl,@RequestParam(value="alt",required=false,defaultValue="0.0") double alt,Principal principal, RedirectAttributes redirectAttributes) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		OAuth2Authentication auth = (OAuth2Authentication) authentication;
		Map<String, String> info = (Map<String, String>)auth.getUserAuthentication().getDetails();
		String userId = info.get("sub");
         
		String uuid = file.getOriginalFilename().split("\\.")[0];
		String fileName = String.valueOf(System.currentTimeMillis()) + "_" + file.getOriginalFilename();
		File adf = new File(AdfManagerApplication.STORAGE_ADF, fileName);
		
		try {
				Files.copy(file.getInputStream(), adf.toPath());
				boolean adfOk = checkAdfFile(adf,uuid);
				if(!adfOk) {
					throw new IOException("Invalid ADF-File "+uuid);
				}
		} catch (IOException e) {
			//Clean-UP
			adf.delete();
			
			return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST).body("Failed. Wrong format");
		}

		AdfDescription newDesc = new AdfDescription(lat, lng, lvl,alt, name, fileName, desc, uuid,userId,AdfDescription.TYPE_ADF);
		service.save(newDesc);

		return ResponseEntity.status(HttpStatus.SC_OK).body("Success.");
	}


	private void unzip(String zipPath, String destFolderPath) throws IOException {
		// From
		// http://www.avajava.com/tutorials/lessons/how-do-i-unzip-the-contents-of-a-zip-file.html
		ZipFile zipFile;
		zipFile = new ZipFile(zipPath);
		File destFolder = new File(destFolderPath);
		if (!destFolder.exists()) {
			destFolder.mkdirs();
		}

		Enumeration<ZipArchiveEntry> enu = zipFile.getEntries();
		while (enu.hasMoreElements()) {
			ZipArchiveEntry zipEntry = enu.nextElement();
			String name = zipEntry.getName();
			long size = zipEntry.getSize();
			long compressedSize = zipEntry.getCompressedSize();
			log.debug("name: %-20s | size: %6d | compressed size: %6d\n", name, size, compressedSize);

			File file = new File(destFolder, name);

			if (name.endsWith("/")) {
				file.mkdirs();
				continue;
			}

			File parent = file.getParentFile();
			if (parent != null) {
				parent.mkdirs();
			}
			InputStream is = zipFile.getInputStream(zipEntry);
			FileOutputStream fos = new FileOutputStream(file);
			byte[] bytes = new byte[1024];
			int length;
			while ((length = is.read(bytes)) >= 0) {
				fos.write(bytes, 0, length);
			}
			is.close();
			fos.close();
		}
		zipFile.close();
	}
	
	private boolean checkAdfFile(File adf,String uuid) throws IOException {
		//Removed code 
		result = true;
		if (result) {
			return true;
		}else{
			return false;
		}
	}

}
