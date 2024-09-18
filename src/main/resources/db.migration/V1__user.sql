CREATE TABLE "user" (
    id uuid PRIMARY KEY,
    email varchar(255) UNIQUE NOT NULL,
    password_hash VARCHAR(45),
    salt VARCHAR(65)
)