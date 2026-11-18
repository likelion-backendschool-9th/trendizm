-- 로컬에서 화면을 확인할 때 쓰는 초기 데이터 예시. 이 파일을 복사해 같은 폴더에 data.sql을 만든다 (data.sql은 커밋하지 않는다).
--   backend/src/main/resources 에서: cp data.example.sql data.sql   (Windows: copy data.example.sql data.sql)
-- application.yml의 spring.sql.init.mode=always라서 서버를 켤 때마다 실행된다.
-- 다시 켜도 중복되지 않게 INSERT마다 ON CONFLICT DO NOTHING을 붙인다.
-- 테스트는 이 데이터를 쓰지 않는다. 테스트 데이터는 각 테스트 코드에서 만든다.

-- member: 일반 회원 1명, 관리자 1명. 비밀번호는 둘 다 password123 (BCrypt로 암호화한 값)
INSERT INTO users (email, password, nickname, role, notify, marketing_agreed, status, created_at, updated_at)
VALUES ('user@trendizm.local', '$2a$10$9qKOP9//7zkcA.SIhrCHdepIR9RSn/Whzc7lCtmOVQq1cnXlqupyq', '일반회원', 'USER', TRUE, FALSE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('admin@trendizm.local', '$2a$10$9qKOP9//7zkcA.SIhrCHdepIR9RSn/Whzc7lCtmOVQq1cnXlqupyq', '관리자', 'ADMIN', TRUE, FALSE, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;
