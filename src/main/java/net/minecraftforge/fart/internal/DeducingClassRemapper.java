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

import org.minecraftplus.srgprocessor.Utils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.MethodRemapper;

import java.util.HashSet;
import java.util.Set;

class DeducingClassRemapper extends ClassRemapper {
    private final DeducingParameterRemapper remapper;
    private final DeducingTransformer transformer;

    DeducingClassRemapper(ClassVisitor classVisitor, DeducingParameterRemapper remapper, DeducingTransformer transformer) {
        super(classVisitor, remapper);
        this.remapper = remapper;
        this.transformer = transformer;
    }

    @Override
    public MethodVisitor visitMethod(
            final int methodAccess,
            final String methodName,
            final String methodDescriptor,
            final String methodSignature,
            final String[] methodExceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(methodAccess, remapper.mapMethodName(className, methodName, methodDescriptor), remapper.mapMethodDesc(methodDescriptor), remapper.mapSignature(methodSignature, false), methodExceptions == null ? null : remapper.mapTypes(methodExceptions));
        if (methodVisitor == null)
            return null;

        return new MethodRemapper(methodVisitor, remapper) {
            boolean isRecordConstructor = false;
            final Set<String> methodUsedNames = new HashSet<>();

            @Override
            public void visitMethodInsn(
                    int opcodeAndSource,
                    String owner,
                    String name,
                    String descriptor,
                    boolean isInterface) {
                isRecordConstructor = owner.equals("java/lang/Record");
                super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
            }

            @Override
            public void visitLocalVariable(
                    final String name,
                    final String descriptor,
                    final String signature,
                    final Label start,
                    final Label end,
                    final int index) {
                String rename = shouldDeduce(name) ? DeducingClassRemapper.this.remapper.deduceParameterName(className, methodName, methodDescriptor, index, name, descriptor) : name;
                super.visitLocalVariable(checkName(rename), descriptor, signature, start, end, index);
            }

            private boolean shouldDeduce(String name) {
                if (isRecordConstructor)
                    return false;

                // Dirty but works...
                return name.startsWith("$$") || name.startsWith("lvt_");
            }

            private String checkName(String name) {
                // Snowmen, added in 1.8.2? Check them names that can exist in source
                if (0x2603 == name.charAt(0))
                    throw new IllegalStateException("Cannot be Snowman here!");

                // Protect against protected java keywords as parameter name
                // Ignore 'this' as it can be 0 index parameter
                if (!name.equals("this") && Utils.JAVA_KEYWORDS.contains(name))
                    throw new IllegalStateException("Parameter name cannot be equal to java keywords: " + name);

                if (name.startsWith("var"))
                    System.err.println(name);

                // Store used name and add number after if duplicates
                int counter = 1;
                String ret = name;
                while (!methodUsedNames.add(ret)) {
                    ret = name + counter;
                    counter++;
                }

                return ret;
            }
        };
    }
}
