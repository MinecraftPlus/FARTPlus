/*
 * Forge Auto Renaming Tool
 * Copyright (c) 2021
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fart.internal;

import net.minecraftforge.fart.api.Inheritance;
import net.minecraftforge.srgutils.IMappingFile;
import org.minecraftplus.srgprocessor.Dictionary;
import org.minecraftplus.srgprocessor.Utils;
import org.objectweb.asm.commons.Remapper;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DeducingParameterRemapper extends Remapper {
    private final Inheritance inh;
    private final Set<Dictionary> dictionaries;
//    private final IMappingFile map;
    private final Consumer<String> log;

//    public DeducingParameterRemapper(Inheritance inh, Set<Dictionary> dictionaries, IMappingFile map, Consumer<String> log) {
    public DeducingParameterRemapper(Inheritance inh, Set<Dictionary> dictionaries,Consumer<String> log) {
        this.inh = inh;
        this.dictionaries = dictionaries;
//        this.map = map;
        this.log = log;
    }

    public String deduceParameterName(final String owner, final String methodName, final String methodDescriptor, final int index, final String paramName, String pdescriptor) {
        // Don't deduce these
        if (paramName.equalsIgnoreCase("this"))
            return paramName;

        // Find parameter type class from descriptor
        String parameterDescriptor = mapDesc(pdescriptor);
        String parameterType;
        Matcher matcher = Utils.DESC.matcher(parameterDescriptor);
        if (matcher.find()) {
            parameterType = matcher.group("cls");
            if (parameterType == null)
                parameterType = matcher.group();
        } else
            throw new IllegalStateException("Invalid prameter descriptor: " + parameterDescriptor);

        // Extract only class name from type
        String parameterName = parameterType.substring(parameterType.lastIndexOf("/") + 1);
        parameterName = parameterName.substring(parameterName.lastIndexOf("$") + 1); // Use last inner class name

        // Add 'a' prefix to parameters which are arrays
        if (parameterDescriptor.startsWith("["))//parameterDescriptor.isArray())
            parameterName = "a" + parameterName;

        // Deduce parameter name from class type and rules in dictionary
        for (org.minecraftplus.srgprocessor.Dictionary dictionary : dictionaries) {
            for (Map.Entry<org.minecraftplus.srgprocessor.Dictionary.Trigger, org.minecraftplus.srgprocessor.Dictionary.Action> rule : dictionary.getRules().entrySet()) {
                org.minecraftplus.srgprocessor.Dictionary.Trigger trigger = rule.getKey();

                Pattern filter = trigger.getFilter();
                if (filter != null && !filter.matcher(parameterType).matches()) {
                    continue; // Skip dictionary replaces if filter not pass
                }

                Pattern pattern = trigger.getPattern();
                Dictionary.Action action = rule.getValue();
                Matcher ruleMatcher = pattern.matcher(parameterName);
                if (ruleMatcher.matches()) { // Only one replace at time
                    parameterName = action.act(ruleMatcher);
                }
            }
        }

        // Always lowercase parameter name
        String deduced = parameterName.toLowerCase(Locale.ROOT);

        return deduced;
    }

    private Inheritance getInheritance() {
        return this.inh;
    }

    private Set<Dictionary> getDictionaries() {
        return this.dictionaries;
    }
}
