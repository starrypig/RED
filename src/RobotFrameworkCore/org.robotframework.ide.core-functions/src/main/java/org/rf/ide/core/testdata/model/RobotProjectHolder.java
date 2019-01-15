/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.testdata.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.rf.ide.core.environment.NullRuntimeEnvironment;
import org.rf.ide.core.environment.RobotRuntimeEnvironment;
import org.rf.ide.core.project.ImportSearchPaths.PathsProvider;
import org.rf.ide.core.project.RobotProjectConfig;
import org.rf.ide.core.testdata.imported.ARobotInternalVariable;
import org.rf.ide.core.testdata.imported.DictionaryRobotInternalVariable;
import org.rf.ide.core.testdata.imported.ListRobotInternalVariable;
import org.rf.ide.core.testdata.imported.ScalarRobotInternalVariable;
import org.rf.ide.core.testdata.importer.ResourceImportReference;
import org.rf.ide.core.testdata.importer.VariablesFileImportReference;

import com.google.common.annotations.VisibleForTesting;

public class RobotProjectHolder {

    private final RobotRuntimeEnvironment robotRuntime;

    private RobotProjectConfig currentConfiguration;

    private final List<RobotFileOutput> readableProjectFiles = new ArrayList<>();

    private final List<ARobotInternalVariable<?>> globalVariables = new ArrayList<>();

    private final Map<String, String> variableMappings = new HashMap<>();

    private final List<File> modulesSearchPaths = new ArrayList<>();

    public RobotProjectHolder() {
        this(new NullRuntimeEnvironment());
    }

    public RobotProjectHolder(final RobotRuntimeEnvironment robotRuntime) {
        this.robotRuntime = robotRuntime;
    }

    public void configure(final RobotProjectConfig configuration, final File projectLocation) {
        if (currentConfiguration != configuration || globalVariables.isEmpty() && variableMappings.isEmpty()) {
            currentConfiguration = configuration;
            setGlobalVariables(map(robotRuntime.getGlobalVariables()));
            setVariableMappings(
                    VariableMappingsResolver.resolve(currentConfiguration.getVariableMappings(), projectLocation));
            setModuleSearchPaths(robotRuntime.getModuleSearchPaths());
        }
    }

    public RobotRuntimeEnvironment getRobotRuntime() {
        return robotRuntime;
    }

    public void setGlobalVariables(final List<ARobotInternalVariable<?>> variables) {
        globalVariables.clear();
        globalVariables.addAll(variables);
    }

    public List<ARobotInternalVariable<?>> getGlobalVariables() {
        return globalVariables;
    }

    public void setVariableMappings(final Map<String, String> mappings) {
        variableMappings.clear();
        variableMappings.putAll(mappings);
    }

    public Map<String, String> getVariableMappings() {
        return variableMappings;
    }

    public void setModuleSearchPaths(final List<File> paths) {
        modulesSearchPaths.clear();
        modulesSearchPaths.addAll(paths);
    }

    public List<File> getModuleSearchPaths() {
        return modulesSearchPaths;
    }

    private List<ARobotInternalVariable<?>> map(final Map<String, Object> varsRead) {
        final List<ARobotInternalVariable<?>> variables = new ArrayList<>();
        for (final String varName : varsRead.keySet()) {
            final Object varValue = varsRead.get(varName);

            if (varValue instanceof List) {
                final List<?> value = (List<?>) varValue;
                variables.add(new ListRobotInternalVariable(varName, value));
            } else if (varValue instanceof Map) {
                final Map<String, Object> value = convert((Map<?, ?>) varValue);
                variables.add(new DictionaryRobotInternalVariable(varName, value));
            } else if (varValue != null && varValue.getClass().isArray()) {
                final List<Object> value = new ArrayList<>();
                final Object[] array = ((Object[]) varValue);
                value.addAll(Arrays.asList(array));
                variables.add(new ListRobotInternalVariable(varName, value));
            } else {
                variables.add(new ScalarRobotInternalVariable(varName, "" + varValue));
            }
        }
        return variables;
    }

    private Map<String, Object> convert(final Map<?, ?> m) {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (final Object key : m.keySet()) {
            map.put("" + key, m.get(key));
        }
        return map;
    }

    public void addModelFile(final RobotFileOutput robotOutput) {
        if (robotOutput != null) {
            final File processedFile = robotOutput.getProcessedFile();
            if (processedFile != null) {
                final RobotFileOutput file = findFileByName(processedFile);
                removeModelFile(file);
            }

            readableProjectFiles.add(robotOutput);
        }
    }

    public void clearModelFiles() {
        readableProjectFiles.clear();
    }

    public void removeModelFile(final RobotFileOutput robotOutput) {
        readableProjectFiles.remove(robotOutput);
    }

    public void addImportedResources(final List<ResourceImportReference> referenced) {
        for (final ResourceImportReference ref : referenced) {
            addImportedResource(ref);
        }
    }

    public void addImportedResource(final ResourceImportReference referenced) {
        readableProjectFiles.add(referenced.getReference());
    }

    public boolean shouldBeLoaded(final RobotFileOutput robotOutput) {
        return robotOutput != null && shouldBeLoaded(robotOutput.getProcessedFile());
    }

    public boolean shouldBeLoaded(final File file) {
        final RobotFileOutput foundFile = findFileByName(file);
        return foundFile == null || file.lastModified() != foundFile.getLastModificationEpochTime();
    }

    public RobotFileOutput findFileWithImportedVariableFile(final PathsProvider pathsProvider,
            final File variableFile) {
        return findFile(new SearchByVariablesImport(pathsProvider, variableFile));
    }

    private class SearchByVariablesImport implements Predicate<RobotFileOutput> {

        private final File toFound;

        private final PathsProvider pathsProvider;

        public SearchByVariablesImport(final PathsProvider pathsProvider, final File toFound) {
            this.pathsProvider = pathsProvider;
            this.toFound = toFound;
        }

        @Override
        public boolean test(final RobotFileOutput robotFile) {
            if (robotFile != null) {
                final List<VariablesFileImportReference> varImports = robotFile
                        .getVariablesImportReferences(RobotProjectHolder.this, pathsProvider);
                for (final VariablesFileImportReference importReference : varImports) {
                    if (importReference.getVariablesFile().getAbsolutePath().equals(toFound.getAbsolutePath())) {
                        return true;
                    }
                }
            }

            return false;
        }

    }

    public RobotFileOutput findFileByName(final File file) {
        return findFile(new SearchByName(file));
    }

    private class SearchByName implements Predicate<RobotFileOutput> {

        private final File toFound;

        public SearchByName(final File toFound) {
            this.toFound = toFound;
        }

        @Override
        public boolean test(final RobotFileOutput robotFile) {
            return robotFile != null && robotFile.getProcessedFile() != null
                    && robotFile.getProcessedFile().getAbsolutePath().equals(toFound.getAbsolutePath());
        }
    }

    @VisibleForTesting
    RobotFileOutput findFile(final Predicate<RobotFileOutput> criteria) {
        for (final RobotFileOutput robotFile : readableProjectFiles) {
            if (criteria.test(robotFile)) {
                return robotFile;
            }
        }
        return null;
    }
}
