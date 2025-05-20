CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    username VARCHAR[255] NOT NULL,
    password VARCHAR[255] NOT NULL,
    role VARCHAR[10]
);