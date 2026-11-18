package com.trendizm.backend;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 루트 AGENTS.md "백엔드 구조 (헥사고날)"의 의존 규칙을 검사한다.
 * com.trendizm.backend 바로 아래 패키지(auth, feed, ...)를 각각 하나의 도메인으로 본다.
 * global의 하위 패키지(global.error, global.response, ...)는 모든 도메인이 import하는 공통 패키지라서 도메인으로 보지 않는다.
 * global 바로 아래 클래스는 도메인처럼 취급해서 다른 도메인이 import하면 위반이 된다.
 */
@DisplayName("헥사고날 아키텍처 규칙")
class ArchitectureTest {

    private static final String ROOT = "com.trendizm.backend";

    private static final String GLOBAL = ROOT + ".global";

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT);
    }

    @Test
    @DisplayName("컨트롤러는 서비스 인터페이스(in 포트)에만 의존하고 ServiceImpl, out 포트, out 어댑터를 참조하지 않는다")
    void 컨트롤러_의존_규칙() {
        noClasses().that().resideInAPackage(ROOT + "..adapter.in.web..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        ROOT + "..application.port.in..",
                        ROOT + "..application.port.out..",
                        ROOT + "..adapter.out..")
                .check(classes);
    }

    @Test
    @DisplayName("application 계층은 adapter 계층을 참조하지 않는다")
    void 서비스_의존_규칙() {
        noClasses().that().resideInAPackage(ROOT + "..application..")
                .should().dependOnClassesThat().resideInAPackage(ROOT + "..adapter..")
                .check(classes);
    }

    @Test
    @DisplayName("XxxServiceImpl은 다른 클래스가 직접 참조하지 않는다")
    void ServiceImpl_직접_참조_금지() {
        noClasses().that().resideOutsideOfPackage(ROOT + "..application.port.in..")
                .should().dependOnClassesThat().resideInAPackage(ROOT + "..application.port.in..")
                .check(classes);
    }

    @Test
    @DisplayName("JpaXxxRepository는 다른 클래스가 직접 참조하지 않는다")
    void JpaRepository_직접_참조_금지() {
        noClasses().that().resideOutsideOfPackage(ROOT + "..adapter.out.persistence..")
                .should().dependOnClassesThat().resideInAPackage(ROOT + "..adapter.out.persistence..")
                .check(classes);
    }

    @Test
    @DisplayName("in 포트(XxxService)는 엔티티 대신 DTO를 주고받는다")
    void in_포트는_엔티티를_노출하지_않는다() {
        noClasses().that().resideInAPackage(ROOT + "..application.port")
                .should().dependOnClassesThat().resideInAPackage(ROOT + "..domain..")
                .check(classes);
    }

    @Test
    @DisplayName("다른 도메인은 ServiceImpl에서 in 포트로만 호출하고, 다른 도메인의 DTO 외에는 참조하지 않는다")
    void 도메인_간_참조_규칙() {
        classes().that().resideInAPackage(ROOT + "..")
                .should(referenceOtherDomainsOnlyThroughInPort())
                .check(classes);
    }

    @Test
    @DisplayName("global은 도메인 패키지를 참조하지 않는다")
    void global_의존_규칙() {
        noClasses().that().resideInAPackage(GLOBAL + "..")
                .should().dependOnClassesThat(
                        resideInAPackage(ROOT + "..").and(resideOutsideOfPackage(GLOBAL + "..")))
                .allowEmptyShould(true) // global이 아직 없는 브랜치에서도 통과하도록
                .check(classes);
    }

    @Test
    @DisplayName("도메인끼리 순환 참조하지 않는다")
    void 도메인_순환_참조_금지() {
        slices().matching(ROOT + ".(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }

    @Test
    @DisplayName("서비스 클래스에는 @Transactional(readOnly = true)를 붙인다")
    void 서비스_트랜잭션_규칙() {
        classes().that().resideInAPackage(ROOT + "..application.port.in..")
                .and().areAnnotatedWith(Service.class)
                .should().beAnnotatedWith(readOnlyTransactional())
                .check(classes);
    }

    @Test
    @DisplayName("JpaXxxRepository는 JpaRepository와 같은 도메인의 out 포트를 함께 상속하는 인터페이스다")
    void JpaRepository_구조_규칙() {
        classes().that().resideInAPackage(ROOT + "..adapter.out.persistence..")
                .should().beInterfaces()
                .andShould().beAssignableTo(JpaRepository.class)
                .andShould(extendOutPortOfSameDomain())
                .check(classes);
    }

    @Test
    @DisplayName("요청 DTO와 응답 DTO는 record다")
    void DTO는_record다() {
        classes().that().resideInAnyPackage(ROOT + "..application.dto..", ROOT + "..adapter.in.web.dto..")
                .should().beAssignableTo(Record.class)
                .check(classes);
    }

    @Test
    @DisplayName("XxxResponse는 같은 도메인 엔티티를 받는 정적 팩토리 from(entity)로 만든다 (같은 도메인에 엔티티가 없으면 제외)")
    void Response는_from으로_만든다() {
        classes().that().resideInAPackage(ROOT + "..application.dto..")
                .and().haveSimpleNameEndingWith("Response")
                .should(haveFromEntityFactory())
                .check(classes);
    }

    @Test
    @DisplayName("엔티티는 protected 기본 생성자와 정적 팩토리 create(...)를 가진다")
    void 엔티티_생성_규칙() {
        classes().that().areAnnotatedWith(Entity.class)
                .should(haveProtectedNoArgConstructor())
                .andShould(haveCreateFactory())
                .check(classes);
    }

    private static ArchCondition<JavaClass> extendOutPortOfSameDomain() {
        return new ArchCondition<>("같은 도메인의 application.port.out 인터페이스를 상속한다") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                String outPortPackage = ROOT + "." + domainOf(javaClass) + ".application.port.out";
                boolean extendsOutPort = javaClass.getRawInterfaces().stream()
                        .anyMatch(parent -> parent.getPackageName().equals(outPortPackage));
                events.add(new SimpleConditionEvent(javaClass, extendsOutPort,
                        javaClass.getName() + "가 " + outPortPackage + "의 인터페이스를 상속하지 않는다"));
            }
        };
    }

    /** 패키지가 basePackage이거나 그 하위 패키지면 true (예: member.domain, member.domain.entity) */
    private static boolean isInPackage(JavaClass javaClass, String basePackage) {
        String packageName = javaClass.getPackageName();
        return packageName.equals(basePackage) || packageName.startsWith(basePackage + ".");
    }

    private static ArchCondition<JavaClass> haveFromEntityFactory() {
        return new ArchCondition<>("public static from(같은 도메인 엔티티)를 가진다") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                String domainPackage = ROOT + "." + domainOf(javaClass) + ".domain";
                // 엔티티가 없는 도메인(예: auth)의 응답은 from(entity)로 만들 수 없으니 검사하지 않는다.
                // 엔티티는 domain/entity에 있으므로 domain과 그 하위 패키지를 모두 본다.
                // domain/enums의 enum 같은 클래스는 엔티티로 세지 않도록 @Entity로 판단한다.
                boolean domainHasEntity = classes.stream()
                        .anyMatch(c -> isInPackage(c, domainPackage) && c.isAnnotatedWith(Entity.class));
                if (!domainHasEntity) {
                    return;
                }
                boolean hasFrom = javaClass.getMethods().stream()
                        .filter(method -> method.getName().equals("from"))
                        .filter(method -> method.getModifiers().containsAll(Set.of(JavaModifier.PUBLIC, JavaModifier.STATIC)))
                        .filter(method -> method.getRawReturnType().equals(javaClass))
                        .anyMatch(method -> method.getRawParameterTypes().size() == 1
                                && isInPackage(method.getRawParameterTypes().get(0), domainPackage)
                                && method.getRawParameterTypes().get(0).isAnnotatedWith(Entity.class));
                events.add(new SimpleConditionEvent(javaClass, hasFrom,
                        javaClass.getName() + "에 public static from(" + domainPackage + "의 엔티티)가 없다"));
            }
        };
    }

    private static ArchCondition<JavaClass> haveProtectedNoArgConstructor() {
        return new ArchCondition<>("protected 기본 생성자를 가진다") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasProtectedNoArg = javaClass.getConstructors().stream()
                        .anyMatch(constructor -> constructor.getRawParameterTypes().isEmpty()
                                && constructor.getModifiers().contains(JavaModifier.PROTECTED));
                events.add(new SimpleConditionEvent(javaClass, hasProtectedNoArg,
                        javaClass.getName() + "에 protected 기본 생성자가 없다"));
            }
        };
    }

    private static ArchCondition<JavaClass> haveCreateFactory() {
        return new ArchCondition<>("public static create(...)를 가진다") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean hasCreate = javaClass.getMethods().stream()
                        .filter(method -> method.getName().equals("create"))
                        .filter(method -> method.getModifiers().containsAll(Set.of(JavaModifier.PUBLIC, JavaModifier.STATIC)))
                        .anyMatch(method -> method.getRawReturnType().equals(javaClass));
                events.add(new SimpleConditionEvent(javaClass, hasCreate,
                        javaClass.getName() + "에 public static create(...)가 없다"));
            }
        };
    }

    /**
     * 다른 도메인 B를 참조할 때 허용하는 경우:
     * - B.application.dto (어느 클래스에서든)
     * - B.application.port의 XxxService 인터페이스 (A.application.port.in의 ServiceImpl에서만)
     */
    private static ArchCondition<JavaClass> referenceOtherDomainsOnlyThroughInPort() {
        return new ArchCondition<>("다른 도메인은 in 포트와 application.dto로만 참조한다") {
            @Override
            public void check(JavaClass origin, ConditionEvents events) {
                String originDomain = domainOf(origin);
                if (originDomain == null) {
                    return;
                }
                for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                    JavaClass target = dependency.getTargetClass().getBaseComponentType();
                    String targetDomain = domainOf(target);
                    if (targetDomain == null || targetDomain.equals(originDomain)) {
                        continue;
                    }
                    if (!isAllowed(origin, originDomain, target, targetDomain)) {
                        events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
                    }
                }
            }
        };
    }

    private static boolean isAllowed(JavaClass origin, String originDomain, JavaClass target, String targetDomain) {
        String targetPackage = target.getPackageName();
        if (targetPackage.equals(ROOT + "." + targetDomain + ".application.dto")) {
            return true;
        }
        boolean targetIsInPort = targetPackage.equals(ROOT + "." + targetDomain + ".application.port");
        boolean originIsServiceImpl = origin.getPackageName().equals(ROOT + "." + originDomain + ".application.port.in");
        return targetIsInPort && originIsServiceImpl;
    }

    /** com.trendizm.backend.<도메인>... 이면 도메인 이름, 루트 패키지·global 하위 패키지·외부 클래스면 null */
    private static String domainOf(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        if (!packageName.startsWith(ROOT + ".") || packageName.startsWith(GLOBAL + ".")) {
            return null;
        }
        String rest = packageName.substring(ROOT.length() + 1);
        int dot = rest.indexOf('.');
        return dot < 0 ? rest : rest.substring(0, dot);
    }

    private static DescribedPredicate<JavaAnnotation<?>> readOnlyTransactional() {
        return new DescribedPredicate<>("@Transactional(readOnly = true)") {
            @Override
            public boolean test(JavaAnnotation<?> annotation) {
                return annotation.getRawType().isEquivalentTo(Transactional.class)
                        && Boolean.TRUE.equals(annotation.get("readOnly").orElse(false));
            }
        };
    }
}
