-- 테스트 데이터. 테스트 클래스에서 @Sql("/test.sql")로 불러오고, @Transactional이라 테스트가 끝나면 롤백된다.
-- id는 자동 생성에 맡기고, 테스트에서는 이메일 등으로 찾아 쓴다.

-- member: 활동 중인 회원 1명, 탈퇴한 회원 1명
INSERT INTO users (email, password, nickname, role, notify, marketing_agreed, created_at, updated_at, deleted_at)
VALUES ('member@test.com', 'encoded-password', '테스트회원', 'USER', TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL),
       ('withdrawn@test.com', 'encoded-password', '탈퇴회원', 'USER', TRUE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
