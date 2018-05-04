package org.hswebframework.web.dev.tools.writer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import lombok.SneakyThrows;
import org.hswebframework.utils.file.FileUtils;

import java.io.File;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0
 */
public class ClassWriter {
    @SneakyThrows
    public static void writeClass(String file, String code) {
        File oldClassFile = new File(file);
        if (oldClassFile.exists()) {
            CompilationUnit old = JavaParser.parse(oldClassFile);
            CompilationUnit newClazz = JavaParser.parse(code);
            Map<String, FieldDeclaration> oldFields = old
                    .findAll(FieldDeclaration.class)
                    .stream()
                    .collect(Collectors.toMap(declaration -> declaration.getVariable(0).getNameAsString(), Function.identity()));


            Map<String, MethodDeclaration> oldMethod = old
                    .findAll(MethodDeclaration.class)
                    .stream()
                    .collect(Collectors.toMap(NodeWithSimpleName::getNameAsString, Function.identity()));

            newClazz.findAll(FieldDeclaration.class).forEach(declaration -> {
                String name = declaration.getVariable(0).getNameAsString();
                if (oldFields.get(name) == null) {
                    VariableDeclarator declarator = declaration.getVariable(0);
                    FieldDeclaration newField = old.getType(0)
                            .addField(declarator.getType(), declarator.getNameAsString(),
                                    declaration.getModifiers().toArray(new Modifier[]{}));

                    declaration.getJavadocComment().ifPresent(newField::setJavadocComment);
                    for (Comment comment : declaration.getAllContainedComments()) {
                        newField.setComment(comment);
                    }
                    for (AnnotationExpr annotationExpr : declaration.getAnnotations()) {
                        newField.addAnnotation(annotationExpr.clone());
                    }
                }
            });
            newClazz.findAll(MethodDeclaration.class).forEach(declaration -> {
                String name = declaration.getNameAsString();
                if (oldMethod.get(name) == null) {
                    MethodDeclaration newMethod = old.getType(0)
                            .addMethod(name, declaration.getModifiers().toArray(new Modifier[]{}));

                    declaration.getJavadocComment().ifPresent(newMethod::setJavadocComment);
                    for (Comment comment : declaration.getAllContainedComments()) {
                        newMethod.setComment(comment);
                    }
                    for (AnnotationExpr annotationExpr : declaration.getAnnotations()) {
                        newMethod.addAnnotation(annotationExpr.clone());
                    }
                }
            });
            code = old.toString();
        }

        FileUtils.writeString2File(code, file, "utf-8");
    }
}
