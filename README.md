# README #

TASC (Terraforming and Station Construction) is a mod for the video game Starsector (see http://fractalsoftworks.com).

TASC adds various features including terraforming planets and constructing space stations.

## Setup ##

The instructions below are intended for those with a software development background but who are new to modding Starsector. Following them will make it quick and easy to start writing code, rebuild TASC and launch Starsector with your changes.

1. These steps will only work if you're using Windows.
2. Install IntelliJ Community Edition (https://www.jetbrains.com/idea/download/?section=windows).
3. Install JDK 1.7 (https://www.oracle.com/java/technologies/javase/javase7-archive-downloads.html).
4. Install Git if you don't already have it (https://git-scm.com/download/win).
5. Open Windows File Explorer and navigate to your Starsector mods folder (C:\Program Files (x86)\Fractal Softworks\Starsector\mods by default).
6. Hold down the shift key and right click inside the mods folder in Windows Explorer. You should see an option in the right click menu to open PowerShell. Click that.
7. Paste the following command into PowerShell and run it: git clone https://github.com/boggledstarsector/tasc.git
8. Once the above command is finished, paste and run the following: git clone https://github.com/Lukas22041/LunaLib.git
9. Using IntelliJ, open the project located at C:\Program Files (x86)\Fractal Softworks\Starsector\mods\tasc.
10. Select Build -> Rebuild Project. If the build is successful you did everything correctly.
11. Launch Starsector and enable TASC. Since the repo was cloned to your mods folder, you can simply make changes, rebuild TASC and restart Starsector, and your new build of TASC will be used.