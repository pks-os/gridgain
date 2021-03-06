/*
 * Copyright 2019 GridGain Systems, Inc. and Contributors.
 *
 * Licensed under the GridGain Community Edition License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gridgain.com/products/software/community-edition/gridgain-community-edition-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.visor.plugin;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Abstract base class for Visor pluggable tabs.
 */
public abstract class VisorPluggableTab extends VisorExtensionPoint {
    /**
     * @param model Visor model.
     */
    protected VisorPluggableTab(VisorPluginModel model) {
        super(model);
    }

    /**
     * Tab and menu icon 16x16 px.
     *
     * @return Plugin icon.
     */
    public abstract ImageIcon icon();

    /**
     * Tab tooltip.
     *
     * @return Tooltip.
     */
    public abstract String tooltip();

    /**
     * Construct content of pluggable tab.
     *
     * @param componentsFactory Factory for creating ready UI blocks, like nodes panel and charts.
     * @param dfltTabBtn Panel with standart tab buttons.
     *
     * @return Pluggable tab content.
     */
    public abstract JPanel createPanel(VisorPluginComponentsFactory componentsFactory, JPanel dfltTabBtn);

    /**
     * Will be executed on tab close.
     */
    public void onClosed() {
        // No-op.
    }
}