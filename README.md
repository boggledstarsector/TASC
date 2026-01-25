# README #

TASC (Terraforming and Station Construction) is a mod for the video game Starsector (see http://fractalsoftworks.com).

TASC adds various features including terraforming planets and constructing space stations. The forum thread is at https://fractalsoftworks.com/forum/index.php?topic=17094.0.

## Development Setup ##

The below steps are intended for use on Windows.

1. Install IntelliJ IDE (the free community version is fine) (https://www.jetbrains.com/idea/).
2. Install JDK 17.0.12 (https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html).
3. Install Git (https://git-scm.com/install/windows).
4. Open the command prompt.
5. cd C:\Program Files (x86)\Fractal Softworks\Starsector\mods
6. git clone https://github.com/boggledstarsector/tasc
7. Download build dependencies for TASC and place them in the mods folder. These are IllustratedEntities, CrewReplacer, SecondInCommand, LunaLib, Ashlib and AotD - Vaults of Knowledge.
8. Using IntelliJ IDE, open the project located at C:\Program Files (x86)\Fractal Softworks\Starsector\mods\tasc. 
9. Select Build -> Rebuild Project. You will probably get errors related to the build dependencies above. Go to File -> Project Structure -> Libraries and ensure the references to the build dependency mods jar files are pointed to the correct file locations. Once these are all correct, you will be able to rebuild the project successfully.
10. Launch Starsector and enable TASC. Since the repo was cloned to your mods folder, you can simply make changes to the code, rebuild TASC and restart Starsector, and your new build of TASC will be used.
11. Create a feature branch and submit a pull request for your changes.
