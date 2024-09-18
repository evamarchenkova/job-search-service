CREATE TABLE user_vacancy (
    user_uuid UUID NOT NULL,
    vacancy_id VARCHAR(30),
    UNIQUE (user_uuid, vacancy_id),
    CONSTRAINT user_uuid_fkey FOREIGN KEY (user_uuid) REFERENCES "user"(id)
)