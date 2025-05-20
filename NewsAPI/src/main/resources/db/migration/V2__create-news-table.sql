CREATE TABLE news(
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    writer_id UUID NOT NULL,
    FOREIGN KEY (writer_id) REFERENCES users(id) ON DELETE CASCADE
);