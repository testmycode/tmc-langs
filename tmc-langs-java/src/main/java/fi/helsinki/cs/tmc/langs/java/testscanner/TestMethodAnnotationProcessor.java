package fi.helsinki.cs.tmc.langs.java.testscanner;

import fi.helsinki.cs.tmc.langs.TestDesc;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

@SupportedSourceVersion(value = SourceVersion.RELEASE_7)
@SupportedAnnotationTypes(value = {"fi.helsinki.cs.tmc.testrunner.Exercise", "org.junit.Test"})
class TestMethodAnnotationProcessor extends AbstractProcessor {

    private final List<TestDesc> testDescs = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(Test.class)) {
            if (element.getKind() == ElementKind.METHOD) {
                String methodName = element.getSimpleName().toString();
                String className = ((TypeElement) (element.getEnclosingElement()))
                        .getQualifiedName()
                        .toString();
                List<String> points = pointsOfTestCase(element);
                if (!points.isEmpty()) {
                    String testName = className + " " + methodName;
                    testDescs.add(new TestDesc(testName, ImmutableList.copyOf(points)));
                }
            }
        }
        return false;
    }

    private List<String> pointsOfTestCase(Element method) {
        List<String> pointNames = new ArrayList<>();
        Optional<String> classAnnotation = getPointsAnnotationValueIfAny(
                method.getEnclosingElement()
        );
        if (classAnnotation.isPresent()) {
            String annotation = classAnnotation.get();
            pointNames.addAll(Arrays.asList(annotation.split(" +")));
        }

        Optional<String> methodAnnotation = getPointsAnnotationValueIfAny(method);
        if (methodAnnotation.isPresent()) {
            String annotation = methodAnnotation.get();
            pointNames.addAll(Arrays.asList(annotation.split(" +")));
        }

        return pointNames;
    }

    private Optional<String> getPointsAnnotationValueIfAny(Element element) {
        for (AnnotationMirror am : element.getAnnotationMirrors()) {
            Name annotationName = am.getAnnotationType().asElement().getSimpleName();
            if (annotationName.contentEquals("Points")) {
                String value = getAnnotationValue(am);
                return Optional.of(value);
            }
        }
        return Optional.absent();
    }

    private String getAnnotationValue(AnnotationMirror am) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> e
                : am.getElementValues().entrySet()) {
            if (e.getKey().getSimpleName().contentEquals("value")) {
                Object value = e.getValue().getValue();
                if (value instanceof String) {
                    return (String) value;
                }
            }
        }
        return "";
    }

    public ImmutableList<TestDesc> getTestDescsSortedByName() {
        List<TestDesc> unsorted = new ArrayList<>(testDescs);
        Collections.sort(unsorted, new Comparator<TestDesc>() {
            @Override
            public int compare(TestDesc desc1, TestDesc desc2) {
                return desc1.name.compareTo(desc2.name);
            }
        });
        return ImmutableList.copyOf(unsorted);
    }
}
