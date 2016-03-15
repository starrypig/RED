/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getFirst;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.rf.ide.core.executor.SuiteExecutor;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;

@SuppressWarnings("PMD.GodClass")
public class RobotLaunchConfiguration {

    static final String TYPE_ID = "org.robotframework.ide.robotLaunchConfiguration";

    private static final String USE_PROJECT_EXECUTOR = "Project executor";
    private static final String EXECUTOR_NAME = "Executor";
    private static final String EXECUTOR_ARGUMENTS_ATTRIBUTE = "Executor arguments";
    private static final String INTERPRETER_ARGUMENTS_ATTRIBUTE = "Interpreter arguments";
    private static final String INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE = "Include option enabled";
    private static final String INCLUDED_TAGS_ATTRIBUTE = "Included tags";
    private static final String EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE = "Exclude option enabled";
    private static final String EXCLUDED_TAGS_ATTRIBUTE = "Excluded tags";
    private static final String PROJECT_NAME_ATTRIBUTE = "Project name";
    private static final String TEST_SUITES_ATTRIBUTE = "Test suites";
    private static final String REMOTE_DEBUG_HOST_ATTRIBUTE = "Remote debug host";
    private static final String REMOTE_DEBUG_PORT_ATTRIBUTE = "Remote debug port";
    private static final String REMOTE_DEBUG_TIMEOUT_ATTRIBUTE = "Remote debug timeout";


    private final ILaunchConfiguration configuration;
    
    static ILaunchConfigurationWorkingCopy createDefault(final ILaunchConfigurationType launchConfigurationType,
            final List<IResource> resources) throws CoreException {

        final String name = resources.size() == 1 ? resources.get(0).getName() : resources.get(0).getProject()
                .getName();
        final String configurationName = DebugPlugin.getDefault().getLaunchManager()
                .generateLaunchConfigurationName(name);
        final ILaunchConfigurationWorkingCopy configuration = launchConfigurationType.newInstance(null,
                configurationName);
        final Map<IResource, List<String>> resourcesMapping = new HashMap<>();
        for (final IResource resource : resources) {
            resourcesMapping.put(resource, new ArrayList<String>());
        }
        fillDefaults(configuration, resourcesMapping);
        configuration.doSave();
        return configuration;
    }

    public static void fillDefaults(final ILaunchConfigurationWorkingCopy launchConfig) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchConfig);
        robotConfig.setExecutor(SuiteExecutor.Python);
        robotConfig.setExecutorArguments("");
        robotConfig.setProjectName("");
        robotConfig.setSuitePaths(new HashMap<String, List<String>>());
        robotConfig.setIsIncludeTagsEnabled(false);
        robotConfig.setIsExcludeTagsEnabled(false);
        robotConfig.setIncludedTags(new ArrayList<String>());
        robotConfig.setExcludedTags(new ArrayList<String>());
        robotConfig.setRemoteDebugHost("");
    }

    private static void fillDefaults(final ILaunchConfigurationWorkingCopy launchConfig,
            final Map<IResource, List<String>> suitesMapping) {
        final RobotLaunchConfiguration robotConfig = new RobotLaunchConfiguration(launchConfig);
        final IProject project = getFirst(suitesMapping.keySet(), null).getProject();
        final RobotProject robotProject = RedPlugin.getModelManager().getModel().createRobotProject(project);
        final SuiteExecutor interpreter = robotProject.getRuntimeEnvironment().getInterpreter();
        robotConfig.setExecutor(interpreter);
        robotConfig.setExecutorArguments("");
        robotConfig.setProjectName(project.getName());
        
        final Map<String, List<String>> suitesNamesMapping = new HashMap<>();
        for (final IResource resource : suitesMapping.keySet()) {
            suitesNamesMapping.put(resource.getProjectRelativePath().toPortableString(), suitesMapping.get(resource));
        }
        robotConfig.setSuitePaths(suitesNamesMapping);
        
        robotConfig.setIsIncludeTagsEnabled(false);
        robotConfig.setIsExcludeTagsEnabled(false);
        robotConfig.setIncludedTags(new ArrayList<String>());
        robotConfig.setExcludedTags(new ArrayList<String>());
        robotConfig.setRemoteDebugHost("");
    }
    
    public RobotLaunchConfiguration(final ILaunchConfiguration config) {
        this.configuration = config;
    }

    public String getName() {
        return configuration.getName();
    }

    private ILaunchConfigurationWorkingCopy asWorkingCopy() {
        return configuration instanceof ILaunchConfigurationWorkingCopy ? (ILaunchConfigurationWorkingCopy) configuration
                : null;
    }

    public void setUsingInterpreterFromProject(final boolean usesProjectExecutor) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(USE_PROJECT_EXECUTOR, usesProjectExecutor);
        }
    }
    
    public void setExecutor(final SuiteExecutor executor) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(EXECUTOR_NAME, executor.name());
        }
    }

    public void setExecutorArguments(final String arguments) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(EXECUTOR_ARGUMENTS_ATTRIBUTE, arguments);
        }
    }

    public void setInterpeterArguments(final String arguments) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(INTERPRETER_ARGUMENTS_ATTRIBUTE, arguments);
        }
    }

    public void setProjectName(final String projectName) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(PROJECT_NAME_ATTRIBUTE, projectName);
        }
    }

    public void setSuitePaths(final Map<String, List<String>> suitesToCases) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            final Map<String, String> suites = Maps.asMap(suitesToCases.keySet(), new Function<String, String>() {
                @Override
                public String apply(final String path) {
                    return Joiner.on("::").join(filter(suitesToCases.get(path), Predicates.notNull()));
                }
            });
            launchCopy.setAttribute(TEST_SUITES_ATTRIBUTE, suites);
        }
    }
    
    public void setIsIncludeTagsEnabled(final boolean isIncludeTagsEnabled) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, isIncludeTagsEnabled);
        }
    }
    
    public void setIncludedTags(final List<String> tags) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(INCLUDED_TAGS_ATTRIBUTE, tags);
        }
    }
    
    public void setIsExcludeTagsEnabled(final boolean isExcludeTagsEnabled) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, isExcludeTagsEnabled);
        }
    }
    
    public void setExcludedTags(final List<String> tags) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(EXCLUDED_TAGS_ATTRIBUTE, tags);
        }
    }
    
    public void setRemoteDebugHost(final String host) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(REMOTE_DEBUG_HOST_ATTRIBUTE, host);
        }
    }
    
    public void setRemoteDebugPort(final int port) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(REMOTE_DEBUG_PORT_ATTRIBUTE, port);
        }
    }
    
    public void setRemoteDebugTimeout(final int timeout) {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        if (launchCopy != null) {
            launchCopy.setAttribute(REMOTE_DEBUG_TIMEOUT_ATTRIBUTE, timeout);
        }
    }

    public boolean isUsingInterpreterFromProject() throws CoreException {
        return configuration.getAttribute(USE_PROJECT_EXECUTOR, true);
    }

    public SuiteExecutor getExecutor() throws CoreException {
        return SuiteExecutor.fromName(configuration.getAttribute(EXECUTOR_NAME, SuiteExecutor.Python.name()));
    }

    public String getExecutorArguments() throws CoreException {
        return configuration.getAttribute(EXECUTOR_ARGUMENTS_ATTRIBUTE, "");
    }

    public String getInterpeterArguments() throws CoreException {
        return configuration.getAttribute(INTERPRETER_ARGUMENTS_ATTRIBUTE, "");
    }

    public String getProjectName() throws CoreException {
        return configuration.getAttribute(PROJECT_NAME_ATTRIBUTE, "");
    }

    public Map<String, List<String>> getSuitePaths() throws CoreException {
        final Map<String, String> mapping = configuration.getAttribute(TEST_SUITES_ATTRIBUTE,
                new HashMap<String, String>());
        final Map<String, List<String>> suitesToTestsMapping = new HashMap<>();
        for (final Entry<String, String> entry : mapping.entrySet()) {
            final List<String> splittedTestNames = Splitter.on("::").omitEmptyStrings().splitToList(entry.getValue());
            suitesToTestsMapping.put(entry.getKey(), splittedTestNames);
        }
        return suitesToTestsMapping;
    }

    public boolean isIncludeTagsEnabled() throws CoreException {
        return configuration.getAttribute(INCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, false);
    }
    
    public List<String> getIncludedTags() throws CoreException {
        return configuration.getAttribute(INCLUDED_TAGS_ATTRIBUTE, new ArrayList<String>());
    }
    
    public boolean isExcludeTagsEnabled() throws CoreException {
        return configuration.getAttribute(EXCLUDE_TAGS_OPTION_ENABLED_ATTRIBUTE, false);
    }
    
    public List<String> getExcludedTags() throws CoreException {
        return configuration.getAttribute(EXCLUDED_TAGS_ATTRIBUTE, new ArrayList<String>());
    }
    
    public String getRemoteDebugHost() throws CoreException {
        return configuration.getAttribute(REMOTE_DEBUG_HOST_ATTRIBUTE, "");
    }
    
    public Optional<Integer> getRemoteDebugPort() throws CoreException {
        if (configuration.hasAttribute(REMOTE_DEBUG_PORT_ATTRIBUTE)) {
            return Optional.of(configuration.getAttribute(REMOTE_DEBUG_PORT_ATTRIBUTE, -1));
        }
        return Optional.absent();
    }
    
    public Optional<Integer> getRemoteDebugTimeout() throws CoreException {
        if (configuration.hasAttribute(REMOTE_DEBUG_TIMEOUT_ATTRIBUTE)) {
            return Optional.of(configuration.getAttribute(REMOTE_DEBUG_TIMEOUT_ATTRIBUTE, -1));
        }
        return Optional.absent();
    }

    public RobotProject getRobotProject() throws CoreException {
        final IProject project = getProject();
        return RedPlugin.getModelManager().getModel().createRobotProject(project);
    }

    private IProject getProject() throws CoreException {
        final String projectName = getProjectName();
        final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (!project.exists()) {
            throw newCoreException("Project '" + projectName + "' cannot be found in workspace", null);
        }
        return project;
    }

    private static CoreException newCoreException(final String message, final Throwable cause) {
        return new CoreException(new Status(IStatus.ERROR, RedPlugin.PLUGIN_ID, message, cause));
    }

    public boolean isSuitableFor(final List<IResource> resources) {
        try {
            for (final IResource resource : resources) {
                final IProject project = resource.getProject();
                if (!getProjectName().equals(project.getName())) {
                    return false;
                }
                boolean exists = false;
                for (final String path : getSuitePaths().keySet()) {
                    final IResource res = project.findMember(Path.fromPortableString(path));
                    if (res != null && res.equals(resource)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    return false;
                }
            }
            return true;
        } catch (final CoreException e) {
            return false;
        }
    }
    
    public static void prepareRerunFailedTestsConfiguration(final ILaunchConfigurationWorkingCopy launchCopy,
            final String outputFilePath) throws CoreException {
        final RobotLaunchConfiguration robotLaunchConfig = new RobotLaunchConfiguration(launchCopy);
        robotLaunchConfig.setExecutorArguments("-R" + " " + outputFilePath);
        robotLaunchConfig.setSuitePaths(new HashMap<String, List<String>>());
    }

    public static ILaunchConfigurationWorkingCopy createLaunchConfigurationForSelectedTestCases(
            final Map<IResource, List<String>> resourcesToTestCases) throws CoreException {

        final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        final String configurationName = manager.generateLaunchConfigurationName("New Configuration");
        final ILaunchConfigurationWorkingCopy configuration = manager.getLaunchConfigurationType(TYPE_ID)
                .newInstance(null, configurationName);

        fillDefaults(configuration, resourcesToTestCases);

        return configuration;
    }
}
