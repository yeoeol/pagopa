# Repository Guidelines

## 프로젝트 구조 및 모듈 구성

이 프로젝트는 Java 25와 Spring Boot 4 기반의 모듈형 모놀리스입니다. 운영 코드는 `src/main/java/com/commerce/pagopa`에 있으며, `catalog`, `ordering`, `payment`, `identity`, `recommendation` 등의 최상위 패키지가 업무 모듈을 나타냅니다. 각 모듈의 표현, 애플리케이션, 도메인, 인프라 계층은 해당 모듈 안에 유지하세요. 모듈 간 협력에는 다른 모듈의 내부 구현 대신 공개 API나 애플리케이션 이벤트를 사용합니다.

설정, Flyway 마이그레이션, Thymeleaf 템플릿, 정적 자원은 `src/main/resources`에 있습니다. 테스트는 `src/test/java`에서 운영 코드의 패키지 구조를 따르며, 공용 픽스처와 Testcontainers 지원 코드는 `support` 패키지에 둡니다. `config/`, `fluentd/`, `k6/`에는 품질 및 운영 관련 파일이 있습니다. `build/`, `.gradle/`, 로그와 로컬 데이터 디렉터리는 생성 결과물이므로 직접 수정하거나 커밋하지 않습니다.

## 빌드, 테스트 및 로컬 실행

저장소에 포함된 Gradle Wrapper를 사용하세요. macOS와 Linux에서는 `gradlew.bat` 대신 `./gradlew`를 사용합니다.

- `.\gradlew.bat clean build`: 전체 컴파일, 정적 검사, 테스트를 실행합니다.
- `.\gradlew.bat test`: JUnit 테스트 전체를 실행합니다. Testcontainers 테스트에는 Docker가 필요합니다.
- `.\gradlew.bat test --tests '*ApplicationModulithTest'`: 지정한 테스트 클래스만 실행합니다.
- `.\gradlew.bat spotlessApply`: Java 코드를 포맷하고 사용하지 않는 import를 제거합니다.
- `.\gradlew.bat bootRun --args="--spring.profiles.active=local"`: MySQL과 필수 환경 변수를 준비한 후 로컬 프로필로 실행합니다.

## 코딩 스타일 및 명명 규칙

`.editorconfig`에 따라 UTF-8, LF 줄바꿈, 파일 끝 개행을 사용합니다. Java는 공백 4칸, YAML은 공백 2칸으로 들여쓰기하며 Java 한 줄은 120자를 넘기지 않습니다. Spotless의 Eclipse 포맷과 import 순서를 따르고, Checkstyle 규칙을 준수하세요. 타입은 `PascalCase`, 메서드와 변수는 `camelCase`, 패키지는 소문자로 작성합니다. `System.out`과 `System.err` 대신 애플리케이션 로거를 사용합니다.

## 테스트 지침

JUnit 5, Spring Boot 테스트 슬라이스, Testcontainers, Spring Modulith 테스트를 사용합니다. 테스트 클래스는 `*Test`로 끝내고, 범위를 명확히 해야 할 때는 `*IntegrationTest` 또는 `*ConcurrencyTest`를 사용하세요. 변경한 모듈에 정상·실패·경계 사례를 추가하고, 개발 중에는 관련 테스트를, 리뷰 전에는 `clean build`를 실행합니다.

## 커밋 및 풀 리퀘스트 지침

최근 커밋 형식에 맞춰 `[Feature]`, `[Test]`, `[Chore]` 뒤에 간결한 한국어 요약과 필요하면 PR 번호를 작성합니다. 커밋은 하나의 목적에 집중하세요. PR에는 변경 동작, 영향받는 모듈, 검증 명령, 관련 이슈를 기록하고 템플릿이나 정적 자원 변경 시 화면 이미지를 첨부합니다. `.env`, 자격 증명, 저장소 키, 로그, 빌드 및 런타임 생성물은 커밋하지 않습니다.

## 에이전트 작업 규칙

- 서버 로직을 구현하기 전에는 반드시 사용자의 명시적인 승인을 받습니다. 사용자가 설계 방안이나 구현 방향만 요청한 경우 서버 코드를 곧바로 변경하지 않습니다.
- 서버 구현 방안이 여러 가지라면 각 대안의 장단점과 트레이드오프를 설명하고, 현재 프로젝트 상황에 가장 적합한 방안을 근거와 함께 추천합니다.
- 서버 구현 방안은 글로만 설명하는게 아닌, 실제 코드로써 보여준다.
- 구현 내용을 설명할 때는 일반적인 예제 코드를 제시하기보다 저장소의 현재 구조와 실제 구현 코드를 확인한 뒤, 해당 코드에 적용할 구체적인 변경 사항을 설명합니다.
- HTML, CSS, JavaScript 등 화면 단 로직은 별도 승인 없이 곧바로 구현할 수 있습니다.
- 환경 변수, 외부 API 키, 토큰, 자격 증명 등 민감정보는 절대 커밋하거나 스테이징하지 않습니다. 추적될 가능성이 있는 민감 파일은 필요에 따라 `.gitignore`에 추가합니다.
- 사용자에게 보내는 모든 답변은 반드시 한국어로 작성합니다.
