## Detection and Correction of Android-Specific Code Smells and Energy Bugs: An Android Lint implementation 

### 1.	Tool usage (extended ‘Android Lint’ tool)

In this section, we explain how to use our extended ‘Android Lint’ tool. The extended ‘Android Lint’ tool is compiled in ![Jar file](mylibrary.jar). It is not necessary for the user to analyze the complete source code every time a code smell/energy bug needs to be checked. Code smells/energy bugs can also be explicitly checked for any part of source code using the ‘Analyze’ tab of the Android Studio IDE. 
 An application is analyzed for Android code smells and energy bugs in the following steps: 

Step 1: The jar file new extended ‘Android Lint’ tool is placed in the .android/lint folder of the Android Studio installation, typically located in user.home unless specified otherwise. Step 1 is performed one time steps and does not need to be repeated for every analysis

Step 2:  In ‘Analyze’ menu, select ‘Inspect code’ > whole project. The extended ‘Android Lint’ analyzes the source code based on all custom rules and displays a list of detected code smells and energy bugs under Android > Lint > Performance. The custom rules could be differentiated from the default rules using the issue IDs (which are same as the Android code smell/energy bug names defined in Annex A).

Step 3: For each detected code smell/energy bug our extended ‘Android Lint’ tool show Lint warnings to developers. Whenever a new line of code is added and if it violates any rule, it is automatically detected and highlighted by extended ‘Android Lint’ tool.

Step 4: Clicking on the warning sign shows a suggestion for correction, if available (see figure 1). In case of improved code smell/energy bugs the new warning/suggestions are shown below the standard suggestions.

Step 5: Correction can be applied directly on the code by a single click on the suggestion box.

#### 1.1 Example

Typical usage of this tool is explained through the following example:
* When the user clicks on Analyze > Inspect Code > Whole Project, Android Lint analyzes the source code based on all rules and displays a list of Issues as shown in Figure 5. 
* Figure 1 also shows refactoring options available to the developer for the energy bug Resource Leak (RL) by highlighting the MediaPlayer instance whose instance has not been released. If the user applies the correction by clicking on this option, the correction is applied in the source code.
* Figure shows the corrected source code for Resource Leak issue, by releasing MediaPlayer instance.

![](images/Fig1.png)

**Fig. 1.**  (Left) List of detected Issues. (Right) Highlighted source code with defect and correction suggestion

