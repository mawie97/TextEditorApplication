CREATE TABLE IF NOT EXISTS documents (
     id UUID PRIMARY KEY,
     text TEXT,
     cursor INTEGER NOT NULL,
     anchor INTEGER NOT NULL,
     preferred_column INTEGER NOT NULL
);