/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.selenium.core.action;

import org.eclipse.che.selenium.core.action.PlatformBasedActions;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.SendKeysAction;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Mac OS based extension of {@link Actions}. Modifies the behavior of {@link SendKeysAction} by replacing key press calls.
 * Unfortunately this may need for that reason, that some of selenium tests may send:
 * <ul>
 * <li>{@link Keys#END}</li>
 * <li>{@link Keys#HOME}</li>
 * <li>{@link Keys#PAGE_DOWN}</li>
 * <li>{@link Keys#PAGE_UP}</li>
 * </ul>
 * which don't work in current operation system. So some tests may fail. but for above key press in Mac OS there are equivalence:
 * <ul>
 * <li>Command+Right</li>
 * <li>Command+Left</li>
 * <li>Command+Down</li>
 * <li>Command+Up</li>
 * </ul>
 * and method may look for non-working key presses from the input array of {@link CharSequence} and replace them with equivalence.
 * So for test it will looks like it runs transparently on any OS.
 * <p>
 * For more information see {@link #modifyCharSequence(CharSequence...)}
 *
 * @author Vlad Zhukovskyi
 */
public class MacOSActions extends PlatformBasedActions {
    public MacOSActions(WebDriver driver) {
        super(driver);
    }

    @Override
    protected CharSequence[] modifyCharSequence(CharSequence... keysToSend) {
        final List<CharSequence> modKeysToSend = newArrayList();

        for (CharSequence charSequence : keysToSend) {
            final String key = charSequence.toString();

            if (Keys.END.toString().equals(key)) {
                modKeysToSend.add(Keys.chord(Keys.COMMAND, Keys.RIGHT));
            } else if (Keys.HOME.toString().equals(key)) {
                modKeysToSend.add(Keys.chord(Keys.COMMAND, Keys.LEFT));
            } else if (Keys.PAGE_UP.toString().equals(key)) {
                modKeysToSend.add(Keys.chord(Keys.COMMAND, Keys.UP));
            } else if (Keys.PAGE_DOWN.toString().equals(key)) {
                modKeysToSend.add(Keys.chord(Keys.COMMAND, Keys.DOWN));
            } else {
                modKeysToSend.add(charSequence);
            }
        }

        return modKeysToSend.toArray(new CharSequence[modKeysToSend.size()]);
    }
}
