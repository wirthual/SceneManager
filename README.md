# SceneManager

### 1. General Information
This repository contains code developed during the "Praxis der Forschung" course on the [KIT](https://kit.edu)

##### Detailed information and instructins can be found under http://scenemanager.teco.edu/

#### 1.1 Repository structure
| Name    |     Content |
|:--------|:-------------------- |
| README.md | This file. Contains just brief information about the structure. All further information for users and developers can be obtained from the website of [SceneManager](http://scenemanager.teco.edu/)|
| server/ | Contains source code of the SceneManager server itself. It provides the website and the API to upload and download ADF-Files. Detailed information you can find [here](http://scenemanager.teco.edu/about.html#section2).|
| android_client/ | Contains the implementation of an android client to upload and download files from SceneManager. This is useful if you want to create your own client or interact with the API. Detailed information you can find [here](http://scenemanager.teco.edu/about.html#section3). |
| editsys/ |Contains an proof of concept implementation of an edit system to place objects in the real world using Google Tango. Detailed information you can find [here](http://scenemanager.teco.edu/about.html#section4 ).|
| LICENSE.md| The license of this project. Its released under [MIT License](https://de.wikipedia.org/wiki/MIT-Lizenz) However, it includes a lot of other libraries, which are published under a different license.  |

#### 1.2 For developers
If you plan to improve SceneManager or want to host your own, you can find detailed information about how to do so in the developers section, which you can find  [here](http://scenemanager.teco.edu/fordeveloper).

### 2. Screenshots
#### 2.1.1 SceneManager Home
![home][home]
#### 2.1.2 SceneManager Manage
![manage][manage]
#### 2.1.3 SceneManager Overview
![overview][overview]
#### 2.1.4  SceneManager Upload
![upload][upload]

#### 2.2 Android Client
![client][client]

#### 2.3 Edit Sys
![edit1][edit1]
![edit2][edit2]
![edit3][edit3]

[home]: screenshots/home.png "Landing page"
[manage]: screenshots/manage.png "manage page"
[upload]: screenshots/upload.png "page for uploading ADFs"
[overview]: screenshots/overview.png "overview over all uploaded ADFs"
[client]: screenshots/Client_full.png "overview of android client"
[edit1]: screenshots/edit1.png "editsys 1"
[edit2]: screenshots/edit2.png "editsys 2"
[edit3]: screenshots/edit3.png "editsys 3"