define({ "api": [
  {
    "type": "delete",
    "url": "/api/adf/:id",
    "title": "Delete ADF Description",
    "name": "DeleteADFDescriptions",
    "group": "ADF",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "boolean",
            "optional": false,
            "field": "True",
            "description": "<p>if deletion was successful, false otherwise</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK\ntrue",
          "type": "boolean"
        }
      ]
    },
    "sampleRequest": [
      {
        "url": "http://localhost:8080/adf/1"
      }
    ],
    "version": "0.0.0",
    "filename": "src/main/java/com/adfmanager/controller/AdfDescriptionController.java",
    "groupTitle": "ADF"
  },
  {
    "type": "get",
    "url": "/api/adf/:id",
    "title": "Request ADF Descriptions by id",
    "name": "GetADFDescription_by_id",
    "group": "ADF",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "Number",
            "optional": false,
            "field": "id",
            "description": "<p>Users unique ID.</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Number",
            "optional": false,
            "field": "id",
            "description": "<p>Unique ID of ADF</p>"
          },
          {
            "group": "Success 200",
            "type": "Number",
            "optional": false,
            "field": "lat",
            "description": "<p>Latitude of location</p>"
          },
          {
            "group": "Success 200",
            "type": "Number",
            "optional": false,
            "field": "lng",
            "description": "<p>Longitude of location</p>"
          },
          {
            "group": "Success 200",
            "type": "Number",
            "optional": false,
            "field": "lvl",
            "description": "<p>Level e.g 1st floor in a building</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "name",
            "description": "<p>Name of the ADF</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "fileName",
            "description": "<p>File Name of the ADF</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "description",
            "description": "<p>Description of the ADF</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "uuid",
            "description": "<p>uuid of ADF, given from Tango service</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response: ",
          "content": "HTTP/1.1 200 OK \n{\n\"id\": 1,\n\"lat\": 49.00790579977143,\n\"lng\": 8.412201404571535,\n\"lvl\": 3,\n\"name\": \"dfd\",\n\"fileName\": \"1509468537814_8aa864dc-6442-40de-b501-e572f1042378.zip\",\n\"description\": \"salkdjflkdsjflkdsajfalkdsjflkdsjfjdsfölksajdflksadjfadsgädsikvflkdsa\",\n\"uuid\": \"8aa864dc-6442-40de-b501-e572f1042378\"\n}",
          "type": "json"
        }
      ]
    },
    "sampleRequest": [
      {
        "url": "http://localhost:8080/1"
      }
    ],
    "version": "0.0.0",
    "filename": "src/main/java/com/adfmanager/controller/AdfDescriptionController.java",
    "groupTitle": "ADF"
  },
  {
    "type": "get",
    "url": "/api/adf",
    "title": "Request ADF Descriptions",
    "name": "GetADFDescriptions",
    "group": "ADF",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Object[]",
            "optional": false,
            "field": "ADF",
            "description": "<p>Descriptions List of available ADF Descriptions</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK \n[\n  {\n    \"id\": 1,\n    \"lat\": 49.00790579977143,\n    \"lng\": 8.412201404571535,\n    \"lvl\": 3,\n    \"name\": \"dfd\",\n    \"fileName\": \"1509468537814_8aa864dc-6442-40de-b501-e572f1042378.zip\",\n    \"description\": \"salkdjflkdsjflkdsajfalkdsjflkdsjfjdsfölksajdflksadjfadsgädsikvflkdsa\",\n    \"uuid\": \"8aa864dc-6442-40de-b501-e572f1042378\"\n  }\n]",
          "type": "json"
        }
      ]
    },
    "sampleRequest": [
      {
        "url": "http://localhost:8080/adf"
      }
    ],
    "version": "0.0.0",
    "filename": "src/main/java/com/adfmanager/controller/AdfDescriptionController.java",
    "groupTitle": "ADF"
  },
  {
    "type": "get",
    "url": "/api/search",
    "title": "Request ADF Descriptions based on description",
    "name": "GetADFDescriptions_by_descripiton",
    "sampleRequest": [
      {
        "url": "http://localhost:8080/search?desc=\"findInDescriptions\""
      }
    ],
    "group": "ADF",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "String",
            "optional": false,
            "field": "desc",
            "description": "<p>String to search in the description of ADF Files</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Object[]",
            "optional": false,
            "field": "ADF",
            "description": "<p>Descriptions including desc in their description</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK \n[\n  {\n    \"id\": 1,\n    \"lat\": 49.00790579977143,\n    \"lng\": 8.412201404571535,\n    \"lvl\": 3,\n    \"name\": \"dfd\",\n    \"fileName\": \"1509468537814_8aa864dc-6442-40de-b501-e572f1042378.zip\",\n    \"description\": \"salkdjflkdsjflkdsajfalkdsjflkdsjfjdsfölksajdflksadjfadsgädsikvflkdsa\",\n    \"uuid\": \"8aa864dc-6442-40de-b501-e572f1042378\"\n  }\n]",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "src/main/java/com/adfmanager/controller/AdfDescriptionController.java",
    "groupTitle": "ADF"
  },
  {
    "type": "get",
    "url": "/api/adf",
    "title": "Request ADF Descriptions near to given location",
    "name": "GetADFDescriptions_by_location",
    "sampleRequest": [
      {
        "url": "http://localhost:8080/nearby?lat=49.012762&lng=8.424176&radius=100000.0"
      }
    ],
    "group": "ADF",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "Number",
            "optional": false,
            "field": "lat",
            "description": "<p>Latitude</p>"
          },
          {
            "group": "Parameter",
            "type": "Number",
            "optional": false,
            "field": "lng",
            "description": "<p>Longitude</p>"
          },
          {
            "group": "Parameter",
            "type": "Number",
            "optional": false,
            "field": "radius",
            "description": "<p>Radius for area, depending on lat and lng to search in</p>"
          },
          {
            "group": "Parameter",
            "type": "Number",
            "optional": true,
            "field": "lvl",
            "description": "<p>Level</p>"
          }
        ]
      }
    },
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Object[]",
            "optional": false,
            "field": "ADF",
            "description": "<p>Descriptions near location</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK \n[\n  {\n    \"id\": 1,\n    \"lat\": 49.00790579977143,\n    \"lng\": 8.412201404571535,\n    \"lvl\": 3,\n    \"name\": \"dfd\",\n    \"fileName\": \"1509468537814_8aa864dc-6442-40de-b501-e572f1042378.zip\",\n    \"description\": \"salkdjflkdsjflkdsajfalkdsjflkdsjfjdsfölksajdflksadjfadsgädsikvflkdsa\",\n    \"uuid\": \"8aa864dc-6442-40de-b501-e572f1042378\"\n  }\n]",
          "type": "json"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "src/main/java/com/adfmanager/controller/AdfDescriptionController.java",
    "groupTitle": "ADF"
  },
  {
    "type": "get",
    "url": "/api/adf/user",
    "title": "Request ADF Descriptions from currently authenticated user",
    "name": "GetADFDescriptions_for_current_user",
    "group": "ADF",
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "type": "Object[]",
            "optional": false,
            "field": "ADF",
            "description": "<p>Descriptions List from user</p>"
          }
        ]
      },
      "examples": [
        {
          "title": "Success-Response:",
          "content": "HTTP/1.1 200 OK \n[\n  {\n    \"id\": 1,\n    \"lat\": 49.00790579977143,\n    \"lng\": 8.412201404571535,\n    \"lvl\": 3,\n    \"name\": \"dfd\",\n    \"fileName\": \"1509468537814_8aa864dc-6442-40de-b501-e572f1042378.zip\",\n    \"description\": \"salkdjflkdsjflkdsajfalkdsjflkdsjfjdsfölksajdflksadjfadsgädsikvflkdsa\",\n    \"uuid\": \"8aa864dc-6442-40de-b501-e572f1042378\"\n  }\n]",
          "type": "json"
        }
      ]
    },
    "sampleRequest": [
      {
        "url": "http://localhost:8080/adf/user"
      }
    ],
    "version": "0.0.0",
    "filename": "src/main/java/com/adfmanager/controller/AdfDescriptionController.java",
    "groupTitle": "ADF"
  },
  {
    "type": "post",
    "url": "/file/upload/scene Upload scene by form. Form must include Name,Description,Latitude,Longitude,Level,File",
    "title": "File needs to be a zip including ADF and Sqlite-Database",
    "name": "File_upload",
    "group": "File",
    "header": {
      "examples": [
        {
          "title": "Request-Example:",
          "content": "Accept-Encoding: gzip, deflate\nContent-Type: multipart/form-data;",
          "type": "String"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "src/main/java/com/adfmanager/controller/FileUploadController.java",
    "groupTitle": "File"
  },
  {
    "type": "post",
    "url": "/file/upload/adf Upload adf by form. Form must include Name,Description,Latitude,Longitude,Level,File",
    "title": "File needs to be a an adf file",
    "name": "File_upload",
    "group": "File",
    "header": {
      "examples": [
        {
          "title": "Request-Example:",
          "content": "Accept-Encoding: gzip, deflate\nContent-Type: multipart/form-data;",
          "type": "String"
        }
      ]
    },
    "version": "0.0.0",
    "filename": "src/main/java/com/adfmanager/controller/FileUploadController.java",
    "groupTitle": "File"
  },
  {
    "type": "get",
    "url": "/file/id/:id",
    "title": "Get Scene by id",
    "name": "Get_Scene_by_id",
    "group": "File",
    "parameter": {
      "fields": {
        "Parameter": [
          {
            "group": "Parameter",
            "type": "Number",
            "optional": false,
            "field": "id",
            "description": "<p>Files unique ID.</p>"
          }
        ]
      }
    },
    "version": "0.0.0",
    "filename": "src/main/java/com/adfmanager/controller/FileUploadController.java",
    "groupTitle": "File"
  },
  {
    "success": {
      "fields": {
        "Success 200": [
          {
            "group": "Success 200",
            "optional": false,
            "field": "varname1",
            "description": "<p>No type.</p>"
          },
          {
            "group": "Success 200",
            "type": "String",
            "optional": false,
            "field": "varname2",
            "description": "<p>With type.</p>"
          }
        ]
      }
    },
    "type": "",
    "url": "",
    "version": "0.0.0",
    "filename": "src/main/resources/static/docapi/main.js",
    "group": "_home_raphael_Documents_Studium_PdF_Workspace_AdfManager_src_main_resources_static_docapi_main_js",
    "groupTitle": "_home_raphael_Documents_Studium_PdF_Workspace_AdfManager_src_main_resources_static_docapi_main_js",
    "name": ""
  }
] });
