<html>
<head>
<link href="PLUGINS_ROOT/org.robotframework.ide.eclipse.main.plugin.doc.user/help/style.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<a href="RED/../../../../help/index.html">RED - Robot Editor User Guide</a> &gt; <a href="RED/../../../../help/user_guide/user_guide.html">User guide</a> &gt; <a href="RED/../../../../help/user_guide/project_config.html">Project configuration</a> &gt; 

<h1>Overview of red.xml file</h1>
<p>The <b>red.xml</b> is a file which stores project related setting - some of those controls editing phase 
	(validation, code assistance etc.) while the other can influence RobotFramework execution.</p>
<p>It is always located in project root directory.</p>
<h2>Creating/recreating red.xml</h2>
<p>The <b>red.xml</b> file is automatically created when Robot project is created (e.g. by using 
	<code>File -> New -> Robot Project</code> action).</p>
<p>In case you're importing Robot files to generic Eclipse project you need to make the project Robot-specific by 
	selecting <code>Robot Framework -> Add Robot nature</code> action from context menu of the project.</p>
<img src="images/add_robot_nature.png"/>
<p>In any case <b>red.xml</b> file shall be visible in Project Explorer in root folder.</p>
<h2>Red.xml parts</h2>
<p>RED provides dedicated editor for <b>red.xml</b> file which is normally opened by double clicking on it in Project
	Explorer. Beside that the file is textual and contains XML content and can be edited by any text editor.</p>
<p>Editor for <b>red.xml</b> is divided into 4 groups in which different aspect of project configuration can be 
	modified. The groups are represented by tabs visible at the bottom of the editor area: 
	<a href="#general">General</a>, <a href="#libraries">Libraries</a>, <a href="#variables">Variables</a> and 
	<a href="#validation">Validation</a>.</p>
<h3 id="general">General tab</h3>
<p>This section allows to choose python interpreter for the project other than the one defined at
	<code><a class="command" href="javascript:executeCommand('org.eclipse.ui.window.preferences(preferencePageId=org.robotframework.ide.eclipse.main.plugin.preferences.installed)')">
	Window -> Preferences -> Robot Framework -> Installed frameworks</a></code> preference page.</p>
<img src="images/red_xml_general_tab.png"/>
<h3 id="libraries">Libraries tab</h3>
<p>This section stores informations about Robot external libraries being used together with the paths required for 
	Python.</p>
<p><b>Libraries</b> part - holds a list of discovered or manually added libraries which are used to generate library
	specifications in order to provide proper validation, content assistance etc. For more information see 
	<a href="libraries.html">Recognizing external libraries in RED</a> topic.</p>
<p><b>Paths</b> - holds settings for user defined <code>PYTHONPATH</code>/<code>CLASSPATH</code> which are used when
	RED is communicating with Robot (e.g. for libraries discovery or variable files imports) as well as when launching
	tests execution. The paths here may be absolute or relative and in latter case the relativity can be changed between
	<code>WORKSPACE</code>/<code>PROJECT</code> locations. For more information see <a href="custom_paths.html">
	Custom PYTHONPATH/CLASSPATH and path relativeness</a> topic.</p>
<img src="images/red_xml_libraries_tab.png"/>
<h3 id="variables">Variables tab</h3>
<p>This section stores settings regarding Robot variables.</p>
<p><b>Variable mappings</b> - in this section values can be assigned to variables in order to help RED resolved 
	paramerized imports. For more information see <a href="variable_mapping.html">Variable mappings</a> topic.</p>
<p><b>Variable files</b> - this section specify variable files which should be visible in global scope. Those 
	global variable files are used during test case edit and validation. For more information see 
	<a href="variable_files.html">Global variable files</a>
	topic.</p>
<img src="images/red_xml_variables_tab.png"/>
<h3 id="validation">Validation tab</h3>
<p>This section allows user to limit validation to selected folder or exclude selected folder(s) from validation. 
	To exclude folder in Project right click on folder and choose <i>Exclude</i> from menu. Files can be also 
	excluded by fixed size [KB]. For more information see <a href="../validation/scope.html">Limiting validation
	scope</a> topic.</p>
<img src="images/red_xml_validation_tab.png"/>
</body>
</html>